package com.petcare.system.controller;

import com.petcare.common.Result;
import com.petcare.security.service.TokenBlacklistService;
import com.petcare.security.util.JwtUtils;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.PhoneLoginDTO;
import com.petcare.system.dto.SendCodeDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.dto.UserRegisterDTO;
import com.petcare.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 是否为开发环境。开发环境下 sendCode 接口会在日志中打印验证码便于调试，
     * 生产环境绝不会在响应中返回验证码。
     */
    @Value("${app.dev-mode:false}")
    private boolean devMode;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResultDTO>> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginResultDTO result = userService.login(dto);
        return ResponseEntity.ok(Result.success(result));
    }

    @PostMapping("/register")
    public ResponseEntity<Result<UserInfoDTO>> register(@Valid @RequestBody UserRegisterDTO dto) {
        UserInfoDTO result = userService.register(dto);
        return ResponseEntity.ok(Result.success("注册成功", result));
    }

    @GetMapping("/info")
    public ResponseEntity<Result<UserInfoDTO>> getUserInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserInfoDTO result = userService.getUserInfo(userId);
        return ResponseEntity.ok(Result.success(result));
    }

    @PostMapping("/send-code")
    public ResponseEntity<Result<Void>> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        userService.sendVerificationCode(dto.getPhone());
        return ResponseEntity.ok(Result.success("验证码已发送", null));
    }

    @PostMapping("/login-by-code")
    public ResponseEntity<Result<LoginResultDTO>> loginByCode(@Valid @RequestBody PhoneLoginDTO dto) {
        LoginResultDTO result = userService.loginByCode(dto);
        return ResponseEntity.ok(Result.success("登录成功", result));
    }

    /**
     * 退出登录 — 将当前 Token 加入黑名单，使其不可再次使用。
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token != null) {
            try {
                String jti = jwtUtils.getTokenId(token);
                long expiration = jwtUtils.getTokenExpirationMillis(token);
                tokenBlacklistService.blacklist(jti, expiration);
                log.info("用户 Token 已撤销: jti={}", jti);
            } catch (Exception e) {
                log.warn("Token 黑名单处理失败（可能已过期）: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(Result.success("已退出登录", null));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
