package com.aaron.aaronaiagent.rbac.service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RbacDataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public RbacDataInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments args) {
        Integer userCount = jdbcTemplate.queryForObject("select count(*) from rbac_user", Integer.class);
        if (userCount != null && userCount > 0) {
            return;
        }

        jdbcTemplate.update("insert into rbac_permission (code, name) values ('dashboard:view', '查看控制台')");
        jdbcTemplate.update("insert into rbac_permission (code, name) values ('ai:chat', '使用 AI 对话')");
        jdbcTemplate.update("insert into rbac_permission (code, name) values ('system:user:list', '查看用户列表')");
        jdbcTemplate.update("insert into rbac_permission (code, name) values ('system:role:list', '查看角色列表')");

        jdbcTemplate.update("insert into rbac_role (code, name) values ('ADMIN', '超级管理员')");
        jdbcTemplate.update("insert into rbac_role (code, name) values ('CONSULTANT', '命理顾问')");

        Long dashboardPermissionId = findId("rbac_permission", "dashboard:view");
        Long aiChatPermissionId = findId("rbac_permission", "ai:chat");
        Long userListPermissionId = findId("rbac_permission", "system:user:list");
        Long roleListPermissionId = findId("rbac_permission", "system:role:list");
        Long adminRoleId = findId("rbac_role", "ADMIN");
        Long consultantRoleId = findId("rbac_role", "CONSULTANT");

        jdbcTemplate.update("insert into rbac_role_permission (role_id, permission_id) values (?, ?)", adminRoleId, dashboardPermissionId);
        jdbcTemplate.update("insert into rbac_role_permission (role_id, permission_id) values (?, ?)", adminRoleId, aiChatPermissionId);
        jdbcTemplate.update("insert into rbac_role_permission (role_id, permission_id) values (?, ?)", adminRoleId, userListPermissionId);
        jdbcTemplate.update("insert into rbac_role_permission (role_id, permission_id) values (?, ?)", adminRoleId, roleListPermissionId);
        jdbcTemplate.update("insert into rbac_role_permission (role_id, permission_id) values (?, ?)", consultantRoleId, dashboardPermissionId);
        jdbcTemplate.update("insert into rbac_role_permission (role_id, permission_id) values (?, ?)", consultantRoleId, aiChatPermissionId);

        jdbcTemplate.update(
                "insert into rbac_user (username, nickname, password_hash, status) values (?, ?, ?, ?)",
                "admin", "系统管理员", passwordEncoder.encode("admin123"), "ACTIVE"
        );
        jdbcTemplate.update(
                "insert into rbac_user (username, nickname, password_hash, status) values (?, ?, ?, ?)",
                "fortune", "命理顾问", passwordEncoder.encode("fortune123"), "ACTIVE"
        );

        Long adminUserId = jdbcTemplate.queryForObject("select id from rbac_user where username = 'admin'", Long.class);
        Long fortuneUserId = jdbcTemplate.queryForObject("select id from rbac_user where username = 'fortune'", Long.class);

        jdbcTemplate.update("insert into rbac_user_role (user_id, role_id) values (?, ?)", adminUserId, adminRoleId);
        jdbcTemplate.update("insert into rbac_user_role (user_id, role_id) values (?, ?)", fortuneUserId, consultantRoleId);

        jdbcTemplate.update("insert into rbac_menu (parent_id, name, path, permission_id, sort_order) values (?, ?, ?, ?, ?)", null, "工作台", "/dashboard", dashboardPermissionId, 1);
        jdbcTemplate.update("insert into rbac_menu (parent_id, name, path, permission_id, sort_order) values (?, ?, ?, ?, ?)", null, "AI 对话", "/fortune-master", aiChatPermissionId, 2);
        jdbcTemplate.update("insert into rbac_menu (parent_id, name, path, permission_id, sort_order) values (?, ?, ?, ?, ?)", null, "用户管理", "/dashboard/users", userListPermissionId, 3);
        jdbcTemplate.update("insert into rbac_menu (parent_id, name, path, permission_id, sort_order) values (?, ?, ?, ?, ?)", null, "角色权限", "/dashboard/roles", roleListPermissionId, 4);
    }

    private Long findId(String tableName, String code) {
        return jdbcTemplate.queryForObject(
                "select id from " + tableName + " where code = ?",
                Long.class,
                code
        );
    }
}
