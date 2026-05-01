package com.aaron.aaronaiagent.controller;

import com.aaron.aaronaiagent.common.ApiResponse;
import com.aaron.aaronaiagent.rbac.dto.AdminUserResponse;
import com.aaron.aaronaiagent.rbac.dto.RoleResponse;
import com.aaron.aaronaiagent.rbac.service.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
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
}
