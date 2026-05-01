package com.aaron.aaronaiagent.controller;

import com.aaron.aaronaiagent.common.ApiResponse;
import com.aaron.aaronaiagent.rbac.dto.AdminUserResponse;
import com.aaron.aaronaiagent.rbac.dto.CreateRoleRequest;
import com.aaron.aaronaiagent.rbac.dto.CreateUserRequest;
import com.aaron.aaronaiagent.rbac.dto.PermissionResponse;
import com.aaron.aaronaiagent.rbac.dto.RoleResponse;
import com.aaron.aaronaiagent.rbac.dto.UpdateRoleRequest;
import com.aaron.aaronaiagent.rbac.dto.UpdateUserRequest;
import com.aaron.aaronaiagent.rbac.service.AuthService;
import com.aaron.aaronaiagent.rbac.service.RbacAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final RbacAdminService rbacAdminService;

    public AdminController(AuthService authService, RbacAdminService rbacAdminService) {
        this.authService = authService;
        this.rbacAdminService = rbacAdminService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<List<AdminUserResponse>> users() {
        return ApiResponse.success(authService.listUsers());
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.success(authService.listRoles());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<List<PermissionResponse>> permissions() {
        return ApiResponse.success(rbacAdminService.listPermissions());
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<AdminUserResponse> createUser(@RequestBody CreateUserRequest request) {
        return ApiResponse.success(authService.toAdminUser(rbacAdminService.createUser(request)));
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<AdminUserResponse> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(authService.toAdminUser(rbacAdminService.updateUser(userId, request)));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<RoleResponse> createRole(@RequestBody CreateRoleRequest request) {
        return ApiResponse.success(authService.toRoleResponse(rbacAdminService.createRole(request)));
    }

    @PutMapping("/roles/{roleCode}")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable String roleCode, @RequestBody UpdateRoleRequest request) {
        return ApiResponse.success(authService.toRoleResponse(rbacAdminService.updateRole(roleCode, request)));
    }
}
