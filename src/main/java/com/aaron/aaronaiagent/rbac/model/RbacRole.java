package com.aaron.aaronaiagent.rbac.model;

import java.util.List;

public record RbacRole(String code, String name, List<RbacPermission> permissions) {
}
