package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record UpdateUserRequest(
        String nickname,
        String password,
        String status,
        List<String> roleCodes
) {
}
