package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record AdminUserResponse(
        Long userId,
        String username,
        String nickname,
        String status,
        List<String> roles
) {
}
