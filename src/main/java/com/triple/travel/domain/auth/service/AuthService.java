package com.triple.travel.domain.auth.service;

import com.triple.travel.common.exception.DuplicateResourceException;
import com.triple.travel.common.exception.EntityNotFoundException;
import com.triple.travel.common.security.JwtTokenProvider;
import com.triple.travel.domain.auth.dto.AuthRequest;
import com.triple.travel.domain.auth.dto.AuthResponse;
import com.triple.travel.domain.user.entity.User;
import com.triple.travel.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse.SignUp signUp(AuthRequest.SignUp req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
        }
        String encoded = passwordEncoder.encode(req.password());
        User user = User.registerLocal(req.email(), req.nickname(), encoded);
        userRepository.save(user);
        return new AuthResponse.SignUp(user.getId(), user.getEmail(), user.getNickname());
    }

    public AuthResponse.Login login(AuthRequest.Login req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getProvider() != User.Provider.LOCAL || user.getPassword() == null) {
            throw new BadCredentialsException("소셜 로그인 계정입니다. 해당 제공자로 로그인해주세요.");
        }
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = tokenProvider.issueAccessToken(user.getId(), user.getEmail());
        return new AuthResponse.Login(
            token,
            tokenProvider.getAccessTokenTtlSeconds(),
            user.getId(),
            user.getEmail(),
            user.getNickname()
        );
    }
}
