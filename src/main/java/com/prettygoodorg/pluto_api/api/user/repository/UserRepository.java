package com.prettygoodorg.pluto_api.api.user.repository;

import com.prettygoodorg.pluto_api.api.auth.enums.OAuthProvider;
import com.prettygoodorg.pluto_api.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);

}
