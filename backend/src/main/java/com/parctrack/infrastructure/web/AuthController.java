package com.parctrack.infrastructure.web;

import com.parctrack.application.dto.auth.*;
import com.parctrack.application.user.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh token")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authenticationService.logout(request != null ? request.refreshToken() : null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/magic-link/request")
    @Operation(summary = "Request a magic link for passwordless login")
    public ResponseEntity<Void> requestMagicLink(@Valid @RequestBody MagicLinkRequest request) {
        authenticationService.requestMagicLink(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/magic-link/verify")
    @Operation(summary = "Verify magic link and login")
    public ResponseEntity<AuthResponse> verifyMagicLink(@RequestParam String token) {
        return ResponseEntity.ok(authenticationService.verifyMagicLink(token));
    }

    @PostMapping("/password-reset")
    @Operation(summary = "Reset user password (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
