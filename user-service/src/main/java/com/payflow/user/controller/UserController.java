package com.payflow.user.controller;

import com.payflow.common.model.response.ApiResponse;
import com.payflow.common.security.UserPrincipal;
import com.payflow.user.dto.UserProfileResponse;
import com.payflow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and administration endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserProfileResponse profile = userService.getUserProfile(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Get user profile by ID (Admin or Self only - IDOR protected)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(
            @PathVariable("userId") UUID userId
    ) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status (Admin only)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable("userId") UUID userId,
            @RequestParam("status") String status
    ) {
        UserProfileResponse profile = userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
