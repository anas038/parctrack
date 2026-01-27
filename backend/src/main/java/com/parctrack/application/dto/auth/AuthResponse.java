package com.parctrack.application.dto.auth;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String email,
            String username,
            String role,
            UUID organizationId,
            String organizationName
    ) {}
}
