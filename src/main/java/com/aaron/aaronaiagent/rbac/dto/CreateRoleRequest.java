package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record CreateRoleRequest(
        String code,
        String name,
        List<String> permissionCodes
) {
}
