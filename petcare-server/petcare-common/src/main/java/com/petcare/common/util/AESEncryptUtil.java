/*
 * Copyright 2026 PetCare Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.petcare.common.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 加密工具类。
 * <p>
 * 用于加密敏感个人信息（如身份证号、手机号等），满足《个人信息保护法》
 * 对敏感个人信息的加密存储要求。
 * </p>
 *
 * <h3>密钥配置</h3>
 * <p>
 * 密钥通过环境变量 {@code AES_KEY} 注入，必须为 32 字节（256 位）的 Base64 编码字符串。
 * 生成方式：{@code openssl rand -base64 32}
 * </p>
 *
 * <h3>安全特性</h3>
 * <ul>
 *   <li>使用 GCM 模式，提供认证加密 (AEAD)，防止篡改</li>
 *   <li>每次加密生成随机 12 字节 IV，避免相同明文产生相同密文</li>
 *   <li>密文格式: Base64(IV[12 bytes] + ciphertext + GCM tag[16 bytes])</li>
 * </ul>
 *
 * @author PetCare
 * @since 1.0.0
 */
public final class AESEncryptUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  // GCM 推荐 12 字节 IV
    private static final int GCM_TAG_LENGTH = 128; // 128-bit 认证标签

    private static SecretKey secretKey;

    static {
        String keyBase64 = System.getenv("AES_KEY");
        if (keyBase64 != null && !keyBase64.isEmpty()) {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            if (keyBytes.length == 32) {
                secretKey = new SecretKeySpec(keyBytes, "AES");
            } else {
                throw new IllegalArgumentException(
                        "AES_KEY 必须为 32 字节（256 位）的 Base64 编码字符串。"
                                + "生成: openssl rand -base64 32");
            }
        }
        // 未配置密钥时 secretKey 为 null，加解密将抛出明确异常
    }

    private AESEncryptUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 通过 Spring 配置初始化密钥（优先级高于环境变量）。
     *
     * <p>由 {@code AesConfig} 在 Spring 上下文启动后调用。
     * 如果此处提供了有效密钥，将覆盖静态初始化块中从环境变量加载的密钥。</p>
     *
     * @param keyBase64 32 字节 Base64 编码的 AES-256 密钥
     * @throws IllegalArgumentException 如果密钥长度不是 32 字节
     */
    public static void configureWithSpring(String keyBase64) {
        if (keyBase64 == null || keyBase64.isBlank()) {
            return;
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "aes.key 必须为 32 字节（256 位）的 Base64 编码字符串。当前长度: "
                            + keyBytes.length + " 字节。生成: openssl rand -base64 32");
        }
        secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 检查加密密钥是否已配置
     */
    public static boolean isConfigured() {
        return secretKey != null;
    }

    /**
     * 加密明文。
     *
     * @param plaintext 明文（不能为空）
     * @return Base64 编码的密文
     * @throws IllegalStateException 如果 AES_KEY 未配置
     */
    public static String encrypt(String plaintext) {
        if (secretKey == null) {
            throw new IllegalStateException(
                    "AES 加密密钥未配置。请在环境变量中设置 AES_KEY="
                            + "（32 字节 Base64，生成: openssl rand -base64 32）。");
        }
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 密文格式: IV(12) + ciphertext + tag(16)
            byte[] encrypted = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, encrypted, GCM_IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    /**
     * 解密密文。
     *
     * @param ciphertext Base64 编码的密文
     * @return 明文
     * @throws IllegalStateException 如果 AES_KEY 未配置
     */
    public static String decrypt(String ciphertext) {
        if (secretKey == null) {
            throw new IllegalStateException(
                    "AES 加密密钥未配置。请在环境变量中设置 AES_KEY="
                            + "（32 字节 Base64，生成: openssl rand -base64 32）。");
        }
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }

        try {
            byte[] encrypted = Base64.getDecoder().decode(ciphertext);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] actualCiphertext = new byte[encrypted.length - GCM_IV_LENGTH];
            System.arraycopy(encrypted, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, GCM_IV_LENGTH, actualCiphertext, 0, actualCiphertext.length);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plaintext = cipher.doFinal(actualCiphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败（密文可能已损坏或密钥不匹配）", e);
        }
    }
}
