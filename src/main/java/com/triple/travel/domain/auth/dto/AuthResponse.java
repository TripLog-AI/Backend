package com.triple.travel.domain.auth.dto;

public class AuthResponse {

    public record SignUp(
        Long userId,
        String email,
        String nickname
    ) {}

    public record Login(
        String accessToken,
        long expiresInSeconds,
        Long userId,
        String email,
        String nickname
    ) {}
}
