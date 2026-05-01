package com.aaron.aaronaiagent.rbac.service;

import com.aaron.aaronaiagent.rbac.model.RbacMenu;
import com.aaron.aaronaiagent.rbac.model.RbacPermission;
import com.aaron.aaronaiagent.rbac.model.RbacRole;
import com.aaron.aaronaiagent.rbac.model.RbacUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcRbacUserStore implements RbacUserStore {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRbacUserStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<RbacUser> findByUsername(String username) {
        return findUsers("where u.username = ?", username).stream().findFirst();
    }

    @Override
    public Optional<RbacUser> findById(Long userId) {
        return findUsers("where u.id = ?", userId).stream().findFirst();
    }

    @Override
    public List<RbacUser> findAllUsers() {
        return findUsers("", new Object[0]);
    }

    @Override
    public List<RbacRole> findAllRoles() {
        String sql = """
                select r.id as role_id,
                       r.code as role_code,
                       r.name as role_name,
                       p.id as permission_id,
                       p.code as permission_code,
                       p.name as permission_name
                  from rbac_role r
             left join rbac_role_permission rp on rp.role_id = r.id
             left join rbac_permission p on p.id = rp.permission_id
              order by r.id, p.id
                """;

        Map<Long, RoleAccumulator> roleMap = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long roleId = rs.getLong("role_id");
            String roleCode = rs.getString("role_code");
            String roleName = rs.getString("role_name");
            RoleAccumulator accumulator = roleMap.get(roleId);
            if (accumulator == null) {
                accumulator = new RoleAccumulator(roleId, roleCode, roleName);
                roleMap.put(roleId, accumulator);
            }
            Long permissionId = rs.getObject("permission_id", Long.class);
            if (permissionId != null) {
                accumulator.addPermission(new RbacPermission(
                        rs.getString("permission_code"),
                        rs.getString("permission_name")
                ));
            }
        });
        return roleMap.values().stream().map(RoleAccumulator::toRole).toList();
    }

    @Override
    public List<RbacMenu> findMenusByUserId(Long userId) {
        String sql = """
                select distinct m.id,
                                m.parent_id,
                                m.name,
                                m.path,
                                p.code as permission_code,
                                m.sort_order,
                                coalesce(m.parent_id, 0) as parent_sort
                  from rbac_menu m
                  join rbac_permission p on p.id = m.permission_id
                  join rbac_role_permission rp on rp.permission_id = p.id
                  join rbac_user_role ur on ur.role_id = rp.role_id
                 where ur.user_id = ?
              order by parent_sort, m.sort_order, m.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new RbacMenu(
                rs.getLong("id"),
                rs.getObject("parent_id", Long.class),
                rs.getString("name"),
                rs.getString("path"),
                rs.getString("permission_code"),
                rs.getInt("sort_order")
        ), userId);
    }

    private List<RbacUser> findUsers(String whereClause, Object... args) {
        String sql = """
                select u.id as user_id,
                       u.username,
                       u.nickname,
                       u.password_hash,
                       u.status,
                       r.id as role_id,
                       r.code as role_code,
                       r.name as role_name,
                       p.id as permission_id,
                       p.code as permission_code,
                       p.name as permission_name
                  from rbac_user u
             left join rbac_user_role ur on ur.user_id = u.id
             left join rbac_role r on r.id = ur.role_id
             left join rbac_role_permission rp on rp.role_id = r.id
             left join rbac_permission p on p.id = rp.permission_id
                %s
              order by u.id, r.id, p.id
                """.formatted(whereClause);

        Map<Long, UserAccumulator> userMap = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long userId = rs.getLong("user_id");
            String username = rs.getString("username");
            String nickname = rs.getString("nickname");
            String passwordHash = rs.getString("password_hash");
            String status = rs.getString("status");
            UserAccumulator accumulator = userMap.get(userId);
            if (accumulator == null) {
                accumulator = new UserAccumulator(userId, username, nickname, passwordHash, status);
                userMap.put(userId, accumulator);
            }

            Long roleId = rs.getObject("role_id", Long.class);
            if (roleId == null) {
                return;
            }

            accumulator.addRole(
                    roleId,
                    rs.getString("role_code"),
                    rs.getString("role_name"),
                    rs.getObject("permission_id", Long.class),
                    rs.getString("permission_code"),
                    rs.getString("permission_name")
            );
        }, args);

        return userMap.values().stream().map(UserAccumulator::toUser).toList();
    }

    private static final class UserAccumulator {

        private final Long id;
        private final String username;
        private final String nickname;
        private final String passwordHash;
        private final String status;
        private final Map<Long, RoleAccumulator> roles = new LinkedHashMap<>();

        private UserAccumulator(Long id, String username, String nickname, String passwordHash, String status) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.passwordHash = passwordHash;
            this.status = status;
        }

        private void addRole(Long roleId, String roleCode, String roleName, Long permissionId, String permissionCode, String permissionName) {
            RoleAccumulator role = roles.computeIfAbsent(roleId, ignored -> new RoleAccumulator(roleId, roleCode, roleName));
            if (permissionId != null) {
                role.addPermission(new RbacPermission(permissionCode, permissionName));
            }
        }

        private RbacUser toUser() {
            return new RbacUser(
                    id,
                    username,
                    nickname,
                    passwordHash,
                    status,
                    roles.values().stream().map(RoleAccumulator::toRole).toList()
            );
        }
    }

    private static final class RoleAccumulator {

        private final Long id;
        private final String code;
        private final String name;
        private final Map<String, RbacPermission> permissions = new LinkedHashMap<>();

        private RoleAccumulator(Long id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        private void addPermission(RbacPermission permission) {
            permissions.put(permission.code(), permission);
        }

        private RbacRole toRole() {
            return new RbacRole(code, name, new ArrayList<>(permissions.values()));
        }
    }
}
