package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record CreateUserRequest(
        String username,
        String nickname,
        String password,
        String status,
        List<String> roleCodes
) {
}
