package com.prettygoodorg.pluto_api.api.user.entity;

import com.fasterxml.uuid.Generators;
import com.prettygoodorg.pluto_api.api.auth.enums.OAuthProvider;
import com.prettygoodorg.pluto_api.api.auth.enums.Role;
import com.prettygoodorg.pluto_api.api.auth.oauth2.OAuthUserInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "terms_agreed_at", nullable = false)
    private LocalDateTime termsAgreedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void assignId() {
        if (this.id == null) {
            this.id = Generators.timeBasedEpochRandomGenerator().generate();
        }
    }

    public static User create(OAuthUserInfo info, LocalDateTime termsAgreedAt) {
        User user = new User();
        user.email = info.getEmail();
        user.nickname = info.getNickname();
        user.profileImg = info.getProfileImg();
        user.provider = OAuthProvider.valueOf(info.getProvider());
        user.providerId = info.getProviderId();
        user.role = Role.USER;
        user.termsAgreedAt = termsAgreedAt;
        return user;
    }

}
