package com.parctrack.infrastructure.persistence;

import com.parctrack.domain.user.User;
import com.parctrack.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {
    @Override
    Optional<User> findByEmail(String email);

    @Override
    @Query("SELECT u FROM User u WHERE u.magicLinkToken = :token AND u.magicLinkExpiresAt > CURRENT_TIMESTAMP")
    Optional<User> findByMagicLinkToken(String token);

    @Override
    List<User> findByOrganizationId(UUID organizationId);

    @Override
    boolean existsByEmail(String email);
}
