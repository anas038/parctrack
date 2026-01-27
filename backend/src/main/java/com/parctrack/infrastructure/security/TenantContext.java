package com.parctrack.infrastructure.security;

import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();

    public static void setCurrentTenant(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    public static UUID getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentUserId(UUID userId) {
        currentUserId.set(userId);
    }

    public static UUID getCurrentUserId() {
        return currentUserId.get();
    }

    public static void clear() {
        currentTenant.remove();
        currentUserId.remove();
    }
}
