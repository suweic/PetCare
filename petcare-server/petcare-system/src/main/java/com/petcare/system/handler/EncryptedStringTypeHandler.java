package com.petcare.system.handler;

import com.petcare.common.util.AESEncryptUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler：对数据库字段进行透明的 AES-256-GCM 加解密。
 * <p>
 * 使用方式：在需要加密的实体字段上添加注解：
 * <pre>{@code
 * @TableField(typeHandler = EncryptedStringTypeHandler.class)
 * private String idCard;
 * }</pre>
 * </p>
 *
 * <p>
 * 如果环境变量 AES_KEY 未配置，加密操作将抛出明确异常，解密操作返回原始密文。
 * 这样在未部署加密密钥的环境中，程序启动不受影响，但写入敏感字段会立即失败。
 * </p>
 *
 * @see AESEncryptUtil
 */
@MappedTypes(String.class)
public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        // 写入数据库前加密
        ps.setString(i, AESEncryptUtil.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decryptIfNeeded(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decryptIfNeeded(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decryptIfNeeded(cs.getString(columnIndex));
    }

    /**
     * 尝试解密。如果密钥未配置，返回原始值（假设数据库中为明文，向前兼容）。
     */
    private String decryptIfNeeded(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (!AESEncryptUtil.isConfigured()) {
            // 未配置密钥：返回原始值（向前兼容明文存储的数据）
            return value;
        }
        try {
            return AESEncryptUtil.decrypt(value);
        } catch (Exception e) {
            // 解密失败：可能数据库中是旧明文数据，返回原始值
            return value;
        }
    }
}
