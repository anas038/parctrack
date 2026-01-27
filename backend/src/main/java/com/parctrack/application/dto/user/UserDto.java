package com.parctrack.application.dto.user;

import com.parctrack.domain.user.Role;
import com.parctrack.domain.user.User;
import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String username,
        Role role,
        UUID organizationId,
        String organizationName,
        boolean locked,
        Instant createdAt
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getOrganization().getId(),
                user.getOrganization().getName(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }
}
