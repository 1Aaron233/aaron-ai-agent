package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record RoleResponse(
        String code,
        String name,
        List<String> permissions
) {
}
