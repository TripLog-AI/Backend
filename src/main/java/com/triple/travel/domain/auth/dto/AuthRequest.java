package com.triple.travel.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {

    public record SignUp(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 1, max = 100) String nickname
    ) {}

    public record Login(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}
}
