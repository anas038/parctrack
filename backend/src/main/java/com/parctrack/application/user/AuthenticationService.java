package com.parctrack.application.user;

import com.parctrack.application.audit.AuditService;
import com.parctrack.application.dto.auth.*;
import com.parctrack.domain.user.RefreshToken;
import com.parctrack.domain.user.RefreshTokenRepository;
import com.parctrack.domain.user.User;
import com.parctrack.domain.user.UserRepository;
import com.parctrack.infrastructure.config.SecurityProperties;
import com.parctrack.infrastructure.security.JwtService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final AuditService auditService;
    private final JavaMailSender mailSender;

    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            SecurityProperties securityProperties,
            AuditService auditService,
            JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
        this.auditService = auditService;
        this.mailSender = mailSender;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (user.isLocked()) {
            throw new AuthenticationException("Account is locked. Please try again later.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new AuthenticationException("Invalid credentials");
        }

        user.resetFailedAttempts();
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user, request.extendedSession());

        auditService.logAuthEvent("LOGIN", user.getId(), user.getOrganization().getId(), "Successful login");

        return new AuthResponse(
                accessToken,
                refreshToken,
                toUserInfo(user)
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.deleteByToken(request.refreshToken());
            throw new AuthenticationException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        if (user.isLocked()) {
            throw new AuthenticationException("Account is locked");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user, refreshToken.isExtendedSession());

        refreshTokenRepository.deleteByToken(request.refreshToken());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                toUserInfo(user)
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }
    }

    @Transactional
    public void requestMagicLink(MagicLinkRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setMagicLinkToken(token);
        user.setMagicLinkExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
        userRepository.save(user);

        sendMagicLinkEmail(user.getEmail(), token);
    }

    @Transactional
    public AuthResponse verifyMagicLink(String token) {
        User user = userRepository.findByMagicLinkToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired magic link"));

        user.setMagicLinkToken(null);
        user.setMagicLinkExpiresAt(null);
        user.resetFailedAttempts();
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user, true);

        auditService.logAuthEvent("MAGIC_LINK_LOGIN", user.getId(), user.getOrganization().getId(), "Magic link login");

        return new AuthResponse(
                accessToken,
                refreshToken,
                toUserInfo(user)
        );
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.resetFailedAttempts();
        userRepository.save(user);

        refreshTokenRepository.deleteByUserId(user.getId());

        auditService.logAuthEvent("PASSWORD_RESET", user.getId(), user.getOrganization().getId(), "Password reset by admin");
    }

    private void handleFailedLogin(User user) {
        user.incrementFailedAttempts();
        if (user.getFailedLoginAttempts() >= securityProperties.getMaxFailedAttempts()) {
            user.lock(Instant.now().plus(Duration.ofMinutes(securityProperties.getLockoutDurationMinutes())));
            auditService.logAuthEvent("ACCOUNT_LOCKED", user.getId(), user.getOrganization().getId(),
                    "Account locked after " + user.getFailedLoginAttempts() + " failed attempts");
        }
        userRepository.save(user);
    }

    private String createRefreshToken(User user, boolean extendedSession) {
        String token = jwtService.generateRefreshToken();
        long expirationMs = jwtService.getRefreshTokenExpiration(extendedSession);
        Instant expiresAt = Instant.now().plusMillis(expirationMs);

        RefreshToken refreshToken = new RefreshToken(user, token, expiresAt, extendedSession);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    private void sendMagicLinkEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("ParcTrack - Magic Link Login");
        message.setText("Click this link to log in: http://localhost:5173/auth/magic-link?token=" + token +
                "\n\nThis link will expire in 15 minutes.");
        mailSender.send(message);
    }

    private AuthResponse.UserInfo toUserInfo(User user) {
        return new AuthResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                user.getOrganization().getId(),
                user.getOrganization().getName()
        );
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
