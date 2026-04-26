package com.triple.travel.domain.auth.controller;

import com.triple.travel.common.dto.ApiResponse;
import com.triple.travel.domain.auth.dto.AuthRequest;
import com.triple.travel.domain.auth.dto.AuthResponse;
import com.triple.travel.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가 — 회원가입, 로그인")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "이메일 회원가입")
    public ApiResponse<AuthResponse.SignUp> signUp(@Valid @RequestBody AuthRequest.SignUp req) {
        return ApiResponse.ok(authService.signUp(req));
    }

    @PostMapping("/login")
    @Operation(summary = "이메일 로그인 — accessToken 발급")
    public ApiResponse<AuthResponse.Login> login(@Valid @RequestBody AuthRequest.Login req) {
        return ApiResponse.ok(authService.login(req));
    }
}
