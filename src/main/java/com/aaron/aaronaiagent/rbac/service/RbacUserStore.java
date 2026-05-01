package com.aaron.aaronaiagent.rbac.service;

import com.aaron.aaronaiagent.rbac.model.RbacPermission;
import com.aaron.aaronaiagent.rbac.model.RbacRole;
import com.aaron.aaronaiagent.rbac.model.RbacUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RbacUserStore {

    private final Map<String, RbacUser> usersByUsername;
    private final Map<Long, RbacUser> usersById;
    private final List<RbacRole> roles;

    public RbacUserStore(PasswordEncoder passwordEncoder) {
        RbacPermission dashboardView = new RbacPermission("dashboard:view", "查看控制台");
        RbacPermission aiChat = new RbacPermission("ai:chat", "使用 AI 对话");
        RbacPermission userList = new RbacPermission("system:user:list", "查看用户列表");
        RbacPermission roleList = new RbacPermission("system:role:list", "查看角色列表");

        RbacRole adminRole = new RbacRole("ADMIN", "超级管理员", List.of(dashboardView, aiChat, userList, roleList));
        RbacRole consultantRole = new RbacRole("CONSULTANT", "命理顾问", List.of(dashboardView, aiChat));

        this.roles = List.of(adminRole, consultantRole);

        RbacUser admin = new RbacUser(
                1L,
                "admin",
                "系统管理员",
                passwordEncoder.encode("admin123"),
                "ACTIVE",
                List.of(adminRole)
        );
        RbacUser consultant = new RbacUser(
                2L,
                "fortune",
                "命理顾问",
                passwordEncoder.encode("fortune123"),
                "ACTIVE",
                List.of(consultantRole)
        );

        this.usersByUsername = new LinkedHashMap<>();
        this.usersByUsername.put(admin.username(), admin);
        this.usersByUsername.put(consultant.username(), consultant);

        this.usersById = new LinkedHashMap<>();
        this.usersById.put(admin.id(), admin);
        this.usersById.put(consultant.id(), consultant);
    }

    public Optional<RbacUser> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<RbacUser> findById(Long userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    public List<RbacUser> findAllUsers() {
        return List.copyOf(usersByUsername.values());
    }

    public List<RbacRole> findAllRoles() {
        return roles;
    }
}
