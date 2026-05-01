package com.aaron.aaronaiagent.controller;

import com.aaron.aaronaiagent.common.ApiResponse;
import com.aaron.aaronaiagent.rbac.dto.LoginRequest;
import com.aaron.aaronaiagent.rbac.dto.LoginResponse;
import com.aaron.aaronaiagent.rbac.dto.MenuResponse;
import com.aaron.aaronaiagent.rbac.dto.UserProfileResponse;
import com.aaron.aaronaiagent.rbac.service.AuthService;
import com.aaron.aaronaiagent.security.SecurityUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ApiResponse.success(authService.login(request.username(), request.password()));
        } catch (BadCredentialsException exception) {
            return ApiResponse.failure(401, exception.getMessage());
        }
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> profile(@AuthenticationPrincipal SecurityUser securityUser) {
        return ApiResponse.success(authService.toProfile(securityUser.getSourceUser()));
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuResponse>> menus(@AuthenticationPrincipal SecurityUser securityUser) {
        UserProfileResponse profile = authService.toProfile(securityUser.getSourceUser());
        return ApiResponse.success(authService.buildMenus(profile));
    }
}
