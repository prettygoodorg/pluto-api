package com.prettygoodorg.pluto_api.api.user.dto.response;

import com.prettygoodorg.pluto_api.api.auth.enums.OAuthProvider;
import com.prettygoodorg.pluto_api.api.user.entity.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponse {
    private UUID id;
    private OAuthProvider provider;
    private String providerId;

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.provider = user.getProvider();
        this.providerId = user.getProviderId();
    }
}
