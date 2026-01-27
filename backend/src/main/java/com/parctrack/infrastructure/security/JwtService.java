package com.parctrack.infrastructure.security;

import com.parctrack.domain.user.User;
import com.parctrack.infrastructure.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;
    private final ResourceLoader resourceLoader;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public JwtService(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        this.jwtProperties = jwtProperties;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey();
            this.publicKey = loadPublicKey();
        } catch (Exception e) {
            log.warn("Could not load RSA keys, generating new ones for development: {}", e.getMessage());
            var keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String keyPath = jwtProperties.getPrivateKeyPath();
        try (InputStream is = resourceLoader.getResource(keyPath).getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        String keyPath = jwtProperties.getPublicKeyPath();
        try (InputStream is = resourceLoader.getResource(keyPath).getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("orgId", user.getOrganization().getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return UUID.fromString(claims.getSubject());
    }

    public UUID extractOrganizationId(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return UUID.fromString(claims.get("orgId", String.class));
    }

    public String extractRole(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return true;
        }
        return claims.getExpiration().before(new Date());
    }

    public long getRefreshTokenExpiration(boolean extendedSession) {
        return extendedSession ? jwtProperties.getExtendedSessionExpiration() : jwtProperties.getRefreshTokenExpiration();
    }
}
