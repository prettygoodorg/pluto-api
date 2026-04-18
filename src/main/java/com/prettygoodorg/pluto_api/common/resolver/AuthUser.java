package com.prettygoodorg.pluto_api.common.resolver;

import java.util.UUID;

public record AuthUser(UUID userId, String role) {
}
