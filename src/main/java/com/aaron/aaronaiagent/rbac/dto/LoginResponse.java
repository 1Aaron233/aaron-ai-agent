package com.aaron.aaronaiagent.rbac.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserProfileResponse user
) {
}
