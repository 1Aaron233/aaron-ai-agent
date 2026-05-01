package com.aaron.aaronaiagent.rbac.model;

public record RbacMenu(
        Long id,
        Long parentId,
        String name,
        String path,
        String permission,
        Integer sortOrder
) {
}
