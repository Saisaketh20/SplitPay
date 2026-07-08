package com.splitwisepay.demo.Controller;

import com.splitwisepay.demo.DTO.Request.UpdateCredRequest;
import com.splitwisepay.demo.DTO.Response.ApiResponse;
import com.splitwisepay.demo.DTO.Response.UserResponse;
import com.splitwisepay.demo.Entity.User;
import com.splitwisepay.demo.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── GET MY PROFILE ───────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal User currentUser) {

        UserResponse response = userService.getMyProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── UPDATE NAME OR PASSWORD ──────────────────────────────────────
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateCredRequest request) {

        UserResponse response = userService.updateProfile(currentUser, request);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", response));
    }

    // ─── SEARCH USER BY EMAIL ─────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserResponse>> searchByEmail(
            @RequestParam String email) {

        UserResponse response = userService.searchByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}