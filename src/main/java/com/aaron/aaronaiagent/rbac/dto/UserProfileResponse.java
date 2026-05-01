package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record UserProfileResponse(
        Long userId,
        String username,
        String nickname,
        String status,
        List<String> roles,
        List<String> permissions
) {
}
