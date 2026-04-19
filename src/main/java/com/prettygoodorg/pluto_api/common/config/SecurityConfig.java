package com.prettygoodorg.pluto_api.common.config;

import com.prettygoodorg.pluto_api.api.auth.oauth2.NoopOAuth2AuthorizedClientRepository;
import com.prettygoodorg.pluto_api.api.auth.oauth2.OAuth2FailureHandler;
import com.prettygoodorg.pluto_api.api.auth.oauth2.OAuth2SuccessHandler;
import com.prettygoodorg.pluto_api.api.auth.oauth2.RedisAuthorizationRequestRepository;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RedisAuthorizationRequestRepository authorizationRequestRepository,
            NoopOAuth2AuthorizedClientRepository authorizedClientRepository,
            OAuth2SuccessHandler oAuth2SuccessHandler,
            OAuth2FailureHandler oAuth2FailureHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(authorizationRequestRepository)
                        )
                        .authorizedClientRepository(authorizedClientRepository)
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.to("health", "info", "livenessState", "readinessState")).permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
