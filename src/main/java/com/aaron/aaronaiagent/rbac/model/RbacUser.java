package com.aaron.aaronaiagent.rbac.model;

import java.util.List;

public record RbacUser(
        Long id,
        String username,
        String nickname,
        String passwordHash,
        String status,
        List<RbacRole> roles
) {
}
