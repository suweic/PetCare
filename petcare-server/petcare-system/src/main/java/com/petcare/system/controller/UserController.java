package com.petcare.system.controller;

import com.petcare.common.Result;
import com.petcare.system.dto.LoginResultDTO;
import com.petcare.system.dto.PhoneLoginDTO;
import com.petcare.system.dto.SendCodeDTO;
import com.petcare.system.dto.UserInfoDTO;
import com.petcare.system.dto.UserLoginDTO;
import com.petcare.system.dto.UserRegisterDTO;
import com.petcare.system.service.UserService;
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
        String code = userService.sendVerificationCode(dto.getPhone());
        if (devMode) {
            log.info("开发模式 — 验证码: phone={}, code={}", dto.getPhone(), code);
        }
        return ResponseEntity.ok(Result.success("验证码已发送", null));
    }

    @PostMapping("/login-by-code")
    public ResponseEntity<Result<LoginResultDTO>> loginByCode(@Valid @RequestBody PhoneLoginDTO dto) {
        LoginResultDTO result = userService.loginByCode(dto);
        return ResponseEntity.ok(Result.success("登录成功", result));
    }
}
