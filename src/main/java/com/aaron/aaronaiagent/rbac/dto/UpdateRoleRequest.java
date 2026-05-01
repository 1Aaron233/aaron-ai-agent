package com.aaron.aaronaiagent.rbac.dto;

import java.util.List;

public record UpdateRoleRequest(
        String name,
        List<String> permissionCodes
) {
}
