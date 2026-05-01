package com.aaron.aaronaiagent.rbac.service;

import com.aaron.aaronaiagent.rbac.dto.AdminUserResponse;
import com.aaron.aaronaiagent.rbac.dto.LoginResponse;
import com.aaron.aaronaiagent.rbac.dto.MenuResponse;
import com.aaron.aaronaiagent.rbac.dto.RoleResponse;
import com.aaron.aaronaiagent.rbac.dto.UserProfileResponse;
import com.aaron.aaronaiagent.rbac.model.RbacMenu;
import com.aaron.aaronaiagent.rbac.model.RbacUser;
import com.aaron.aaronaiagent.security.JwtTokenProvider;
import com.aaron.aaronaiagent.security.SecurityUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final RbacUserStore rbacUserStore;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(RbacUserStore rbacUserStore, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.rbacUserStore = rbacUserStore;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(String username, String password) {
        RbacUser user = rbacUserStore.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("用户名或密码错误"));
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        SecurityUser securityUser = new SecurityUser(user);
        return new LoginResponse(
                jwtTokenProvider.generateToken(securityUser),
                "Bearer",
                jwtTokenProvider.getExpireSeconds(),
                toProfile(user)
        );
    }

    public UserProfileResponse toProfile(RbacUser user) {
        return new UserProfileResponse(
                user.id(),
                user.username(),
                user.nickname(),
                user.status(),
                user.roles().stream().map(role -> role.code()).toList(),
                user.roles().stream()
                        .flatMap(role -> role.permissions().stream())
                        .map(permission -> permission.code())
                        .distinct()
                        .sorted()
                        .toList()
        );
    }

    public List<AdminUserResponse> listUsers() {
        return rbacUserStore.findAllUsers().stream()
                .map(this::toAdminUser)
                .sorted(Comparator.comparing(AdminUserResponse::userId))
                .toList();
    }

    public List<RoleResponse> listRoles() {
        return rbacUserStore.findAllRoles().stream()
                .map(this::toRoleResponse)
                .toList();
    }

    public AdminUserResponse toAdminUser(RbacUser user) {
        return new AdminUserResponse(
                user.id(),
                user.username(),
                user.nickname(),
                user.status(),
                user.roles().stream().map(role -> role.code()).toList(),
                user.roles().stream().map(role -> role.name()).toList()
        );
    }

    public RoleResponse toRoleResponse(com.aaron.aaronaiagent.rbac.model.RbacRole role) {
        return new RoleResponse(
                role.code(),
                role.name(),
                role.permissions().stream().map(permission -> permission.code()).sorted().toList()
        );
    }

    public List<MenuResponse> buildMenus(Long userId) {
        List<RbacMenu> menus = rbacUserStore.findMenusByUserId(userId);
        Map<Long, MutableMenu> menuMap = new LinkedHashMap<>();
        List<MutableMenu> roots = new java.util.ArrayList<>();

        for (RbacMenu menu : menus) {
            MutableMenu current = menuMap.computeIfAbsent(menu.id(), ignored -> new MutableMenu(
                    menu.id(),
                    menu.parentId(),
                    menu.name(),
                    menu.path(),
                    menu.permission()
            ));
            if (menu.parentId() == null) {
                if (!roots.contains(current)) {
                    roots.add(current);
                }
                continue;
            }
            MutableMenu parent = menuMap.get(menu.parentId());
            if (parent == null) {
                parent = new MutableMenu(menu.parentId(), null, "", "", "");
                menuMap.put(menu.parentId(), parent);
            }
            if (!parent.children.contains(current)) {
                parent.children.add(current);
            }
        }

        return roots.stream().map(MutableMenu::toResponse).toList();
    }

    private static final class MutableMenu {

        private final Long id;
        private final Long parentId;
        private final String name;
        private final String path;
        private final String permission;
        private final List<MutableMenu> children = new java.util.ArrayList<>();

        private MutableMenu(Long id, Long parentId, String name, String path, String permission) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.path = path;
            this.permission = permission;
        }

        private MenuResponse toResponse() {
            return new MenuResponse(name, path, permission, children.stream().map(MutableMenu::toResponse).toList());
        }
    }
}
