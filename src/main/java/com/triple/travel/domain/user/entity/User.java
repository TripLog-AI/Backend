package com.triple.travel.domain.user.entity;

import com.triple.travel.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "users",
    indexes = @Index(name = "idx_users_email", columnList = "email")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String nickname;

    /** 소셜 로그인(LOCAL 외)은 null. LOCAL은 BCrypt 해시 저장. */
    @Column(length = 100)
    private String password;

    @Column(length = 512)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(length = 255)
    private String providerId;

    @Builder
    private User(String email, String nickname, String password, String profileImageUrl,
                 Provider provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static User registerLocal(String email, String nickname, String encodedPassword) {
        return User.builder()
            .email(email)
            .nickname(nickname)
            .password(encodedPassword)
            .provider(Provider.LOCAL)
            .build();
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public enum Provider {
        LOCAL, GOOGLE, KAKAO, APPLE
    }
}
