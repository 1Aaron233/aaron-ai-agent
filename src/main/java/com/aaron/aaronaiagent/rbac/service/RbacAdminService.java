package com.aaron.aaronaiagent.rbac.service;

import com.aaron.aaronaiagent.rbac.dto.CreateRoleRequest;
import com.aaron.aaronaiagent.rbac.dto.CreateUserRequest;
import com.aaron.aaronaiagent.rbac.dto.PermissionResponse;
import com.aaron.aaronaiagent.rbac.dto.UpdateRoleRequest;
import com.aaron.aaronaiagent.rbac.dto.UpdateUserRequest;
import com.aaron.aaronaiagent.rbac.model.RbacRole;
import com.aaron.aaronaiagent.rbac.model.RbacUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RbacAdminService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final RbacUserStore rbacUserStore;

    public RbacAdminService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, RbacUserStore rbacUserStore) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.rbacUserStore = rbacUserStore;
    }

    public List<PermissionResponse> listPermissions() {
        return jdbcTemplate.query(
                "select code, name from rbac_permission order by id",
                (rs, rowNum) -> new PermissionResponse(rs.getString("code"), rs.getString("name"))
        );
    }

    @Transactional
    public RbacUser createUser(CreateUserRequest request) {
        validateCreateUserRequest(request);
        if (Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select exists(select 1 from rbac_user where username = ?)",
                Boolean.class,
                request.username()
        ))) {
            throw new IllegalArgumentException("用户名已存在");
        }

        jdbcTemplate.update(
                "insert into rbac_user (username, nickname, password_hash, status) values (?, ?, ?, ?)",
                request.username(),
                request.nickname(),
                passwordEncoder.encode(request.password()),
                normalizeStatus(request.status())
        );

        Long userId = jdbcTemplate.queryForObject(
                "select id from rbac_user where username = ?",
                Long.class,
                request.username()
        );
        replaceUserRoles(userId, request.roleCodes());
        return rbacUserStore.findById(userId).orElseThrow();
    }

    @Transactional
    public RbacUser updateUser(Long userId, UpdateUserRequest request) {
        RbacUser existingUser = rbacUserStore.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        List<String> roleCodes = sanitizeCodes(request.roleCodes());
        if (roleCodes.isEmpty()) {
            throw new IllegalArgumentException("至少分配一个角色");
        }

        jdbcTemplate.update(
                "update rbac_user set nickname = ?, status = ?, password_hash = coalesce(?, password_hash), updated_at = current_timestamp where id = ?",
                StringUtils.hasText(request.nickname()) ? request.nickname().trim() : existingUser.nickname(),
                normalizeStatus(request.status()),
                StringUtils.hasText(request.password()) ? passwordEncoder.encode(request.password()) : null,
                userId
        );
        replaceUserRoles(userId, roleCodes);
        return rbacUserStore.findById(userId).orElseThrow();
    }

    @Transactional
    public RbacRole createRole(CreateRoleRequest request) {
        validateCreateRoleRequest(request);
        if (Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select exists(select 1 from rbac_role where code = ?)",
                Boolean.class,
                request.code().trim().toUpperCase()
        ))) {
            throw new IllegalArgumentException("角色编码已存在");
        }

        String roleCode = request.code().trim().toUpperCase();
        jdbcTemplate.update(
                "insert into rbac_role (code, name) values (?, ?)",
                roleCode,
                request.name().trim()
        );
        Long roleId = jdbcTemplate.queryForObject("select id from rbac_role where code = ?", Long.class, roleCode);
        replaceRolePermissions(roleId, request.permissionCodes());
        return findRoleByCode(roleCode);
    }

    @Transactional
    public RbacRole updateRole(String roleCode, UpdateRoleRequest request) {
        String normalizedRoleCode = roleCode.trim().toUpperCase();
        Long roleId = jdbcTemplate.query(
                "select id from rbac_role where code = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                normalizedRoleCode
        );
        if (roleId == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        List<String> permissionCodes = sanitizeCodes(request.permissionCodes());
        if (permissionCodes.isEmpty()) {
            throw new IllegalArgumentException("角色至少需要一个权限");
        }
        jdbcTemplate.update(
                "update rbac_role set name = ? where id = ?",
                StringUtils.hasText(request.name()) ? request.name().trim() : normalizedRoleCode,
                roleId
        );
        replaceRolePermissions(roleId, permissionCodes);
        return findRoleByCode(normalizedRoleCode);
    }

    private void validateCreateUserRequest(CreateUserRequest request) {
        if (!StringUtils.hasText(request.username()) || !StringUtils.hasText(request.nickname()) || !StringUtils.hasText(request.password())) {
            throw new IllegalArgumentException("用户名、昵称和密码不能为空");
        }
        if (sanitizeCodes(request.roleCodes()).isEmpty()) {
            throw new IllegalArgumentException("至少分配一个角色");
        }
    }

    private void validateCreateRoleRequest(CreateRoleRequest request) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.name())) {
            throw new IllegalArgumentException("角色编码和名称不能为空");
        }
        if (sanitizeCodes(request.permissionCodes()).isEmpty()) {
            throw new IllegalArgumentException("至少分配一个权限");
        }
    }

    private void replaceUserRoles(Long userId, List<String> roleCodes) {
        List<Long> roleIds = resolveIds("rbac_role", "code", sanitizeCodes(roleCodes));
        if (roleIds.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }
        jdbcTemplate.update("delete from rbac_user_role where user_id = ?", userId);
        roleIds.forEach(roleId -> jdbcTemplate.update(
                "insert into rbac_user_role (user_id, role_id) values (?, ?)",
                userId,
                roleId
        ));
    }

    private void replaceRolePermissions(Long roleId, List<String> permissionCodes) {
        List<Long> permissionIds = resolveIds("rbac_permission", "code", sanitizeCodes(permissionCodes));
        if (permissionIds.isEmpty()) {
            throw new IllegalArgumentException("权限不存在");
        }
        jdbcTemplate.update("delete from rbac_role_permission where role_id = ?", roleId);
        permissionIds.forEach(permissionId -> jdbcTemplate.update(
                "insert into rbac_role_permission (role_id, permission_id) values (?, ?)",
                roleId,
                permissionId
        ));
    }

    private List<Long> resolveIds(String tableName, String codeColumn, List<String> codes) {
        Map<String, Long> idsByCode = new LinkedHashMap<>();
        for (String code : codes) {
            Long id = jdbcTemplate.query(
                    "select id from " + tableName + " where " + codeColumn + " = ?",
                    rs -> rs.next() ? rs.getLong("id") : null,
                    code
            );
            if (id != null) {
                idsByCode.put(code, id);
            }
        }
        if (idsByCode.size() != codes.size()) {
            throw new IllegalArgumentException("存在无效的编码参数");
        }
        return List.copyOf(idsByCode.values());
    }

    private List<String> sanitizeCodes(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        return codes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(code -> code.contains(":") ? code : code.toUpperCase())
                .distinct()
                .toList();
    }

    private String normalizeStatus(String status) {
        return StringUtils.hasText(status) ? status.trim().toUpperCase() : "ACTIVE";
    }

    private RbacRole findRoleByCode(String roleCode) {
        return rbacUserStore.findAllRoles().stream()
                .filter(role -> role.code().equalsIgnoreCase(roleCode))
                .findFirst()
                .orElseThrow();
    }
}
