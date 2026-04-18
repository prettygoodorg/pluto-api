package com.prettygoodorg.pluto_api.api.user.controller;

import com.prettygoodorg.pluto_api.api.user.dto.response.UserProfileResponse;
import com.prettygoodorg.pluto_api.api.user.service.UserQueryService;
import com.prettygoodorg.pluto_api.common.resolver.AuthUser;
import com.prettygoodorg.pluto_api.common.resolver.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserQueryService userQueryService;

    @GetMapping("me")
    public UserProfileResponse getMyProfile(@CurrentUser AuthUser authUser) {
        return userQueryService.getMyProfile(authUser.userId());
    }

}
