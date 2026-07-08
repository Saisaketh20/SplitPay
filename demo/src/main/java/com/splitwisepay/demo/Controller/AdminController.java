package com.splitwisepay.demo.Controller;

import com.splitwisepay.demo.DTO.Request.LoginRequest;
import com.splitwisepay.demo.DTO.Request.RegisterRequest;
import com.splitwisepay.demo.DTO.Response.AdminStatsResponse;
import com.splitwisepay.demo.DTO.Response.ApiResponse;
import com.splitwisepay.demo.DTO.Response.AuthResponse;
import com.splitwisepay.demo.DTO.Response.UserResponse;
import com.splitwisepay.demo.Service.AdminService;
import com.splitwisepay.demo.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> adminRegister(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = adminService.adminRegister(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = adminService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Admin Login successful", response));
    }

    //-------------------------------------------------------



    // ─────────────────────────────────────────────────────────────
    // GET ALL USERS
    // GET /api/v1/admin/users
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<UserResponse> users = adminService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users fetched successfully",
                        users
                )
        );
    }

    // ─────────────────────────────────────────────────────────────
    // DEACTIVATE USER
    // PUT /api/v1/admin/users/{id}/deactivate
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>>
    deactivateUser(@PathVariable Long id) {

        UserResponse response =
                adminService.deactivateUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User deactivated successfully",
                        response
                )
        );
    }

    // ─────────────────────────────────────────────────────────────
    // GET STATS
    // GET /api/v1/admin/stats
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>>
    getStats() {

        AdminStatsResponse stats =
                adminService.getStats();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admin stats fetched successfully",
                        stats
                )
        );
    }
}