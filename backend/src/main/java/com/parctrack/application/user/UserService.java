package com.parctrack.application.user;

import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.user.CreateUserRequest;
import com.parctrack.application.dto.user.UserDto;
import com.parctrack.domain.organization.Organization;
import com.parctrack.domain.organization.OrganizationRepository;
import com.parctrack.domain.user.User;
import com.parctrack.domain.user.UserRepository;
import com.parctrack.infrastructure.security.TenantContext;
import com.parctrack.infrastructure.web.GlobalExceptionHandler.BusinessException;
import com.parctrack.infrastructure.web.GlobalExceptionHandler.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        UUID orgId = TenantContext.getCurrentTenant();
        return userRepository.findByOrganizationId(orgId).stream()
                .map(UserDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUser(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        User user = userRepository.findById(id)
                .filter(u -> u.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDto.from(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("User with this email already exists");
        }

        UUID orgId = TenantContext.getCurrentTenant();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        User user = new User(organization, request.email(), request.username(), request.role());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user = userRepository.save(user);
        auditService.logAction("USER_CREATED", "User", user.getId());

        return UserDto.from(user);
    }

    @Transactional
    public void unlockUser(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        User user = userRepository.findById(id)
                .filter(u -> u.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.resetFailedAttempts();
        userRepository.save(user);
        auditService.logAction("USER_UNLOCKED", "User", user.getId());
    }

    @Transactional
    public void deleteUser(UUID id) {
        UUID orgId = TenantContext.getCurrentTenant();
        User user = userRepository.findById(id)
                .filter(u -> u.getOrganization().getId().equals(orgId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.deleteById(id);
        auditService.logAction("USER_DELETED", "User", id);
    }
}
