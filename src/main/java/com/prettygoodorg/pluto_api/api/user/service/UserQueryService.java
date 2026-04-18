package com.prettygoodorg.pluto_api.api.user.service;

import com.prettygoodorg.pluto_api.api.user.dto.response.UserProfileResponse;
import com.prettygoodorg.pluto_api.api.user.entity.User;
import com.prettygoodorg.pluto_api.api.user.repository.UserRepository;
import com.prettygoodorg.pluto_api.common.exception.BizException;
import com.prettygoodorg.pluto_api.common.exception.ErrorCodeImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;

    public UserProfileResponse getMyProfile(UUID userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCodeImpl.USER_NOT_FOUND));

        return new UserProfileResponse(me);
    }
}
