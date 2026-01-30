package com.parctrack.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndOrganizationId(String email, UUID organizationId);
    Optional<User> findByMagicLinkToken(String token);
    List<User> findByOrganizationId(UUID organizationId);
    boolean existsByEmail(String email);
    void deleteById(UUID id);
}
