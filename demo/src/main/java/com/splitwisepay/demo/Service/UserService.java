package com.splitwisepay.demo.Service;

import com.splitwisepay.demo.DTO.Request.UpdateCredRequest;
import com.splitwisepay.demo.DTO.Response.UserResponse;
import com.splitwisepay.demo.Entity.User;
import com.splitwisepay.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── GET MY PROFILE ───────────────────────────────────────────────
    public UserResponse getMyProfile(User currentUser) {
        return mapToUserResponse(currentUser);
    }

    // ─── UPDATE NAME OR PASSWORD ──────────────────────────────────────
    public UserResponse updateProfile(User currentUser,
                                      UpdateCredRequest request) {

        // Update name only if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            currentUser.setName(request.getName());
        }

        // Update password only if provided — always BCrypt hash it
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            currentUser.setPasswordHash(
                    passwordEncoder.encode(request.getPassword()));
        }

        // Save updated user to DB
        User updatedUser = userRepository.save(currentUser);
        return mapToUserResponse(updatedUser);
    }

    // ─── SEARCH USER BY EMAIL (for adding to group) ───────────────────
    public UserResponse searchByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "No user found with email: " + email));

        return mapToUserResponse(user);
    }

    // ─── PRIVATE HELPER ───────────────────────────────────────────────
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name()) // ← .name() not .getName()
                .isActive(user.getIsActive())
                .build();
    }
}