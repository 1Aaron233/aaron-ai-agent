package com.aaron.aaronaiagent.rbac.service;

import com.aaron.aaronaiagent.rbac.dto.AdminUserResponse;
import com.aaron.aaronaiagent.rbac.dto.LoginResponse;
import com.aaron.aaronaiagent.rbac.dto.MenuResponse;
import com.aaron.aaronaiagent.rbac.dto.RoleResponse;
import com.aaron.aaronaiagent.rbac.dto.UserProfileResponse;
import com.aaron.aaronaiagent.rbac.model.RbacUser;
import com.aaron.aaronaiagent.security.JwtTokenProvider;
import com.aaron.aaronaiagent.security.SecurityUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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
                .map(user -> new AdminUserResponse(
                        user.id(),
                        user.username(),
                        user.nickname(),
                        user.status(),
                        user.roles().stream().map(role -> role.name()).toList()
                ))
                .sorted(Comparator.comparing(AdminUserResponse::userId))
                .toList();
    }

    public List<RoleResponse> listRoles() {
        return rbacUserStore.findAllRoles().stream()
                .map(role -> new RoleResponse(
                        role.code(),
                        role.name(),
                        role.permissions().stream().map(permission -> permission.code()).sorted().toList()
                ))
                .toList();
    }

    public List<MenuResponse> buildMenus(UserProfileResponse profile) {
        MenuResponse dashboardMenu = new MenuResponse("工作台", "/dashboard", "dashboard:view", List.of());
        MenuResponse aiMenu = new MenuResponse("AI 对话", "/fortune-master", "ai:chat", List.of());
        MenuResponse userMenu = new MenuResponse("用户管理", "/dashboard/users", "system:user:list", List.of());
        MenuResponse roleMenu = new MenuResponse("角色权限", "/dashboard/roles", "system:role:list", List.of());
        return List.of(dashboardMenu, aiMenu, userMenu, roleMenu).stream()
                .filter(menu -> profile.permissions().contains(menu.permission()))
                .toList();
    }
}
