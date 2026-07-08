package com.splitwisepay.demo.Service;



import com.splitwisepay.demo.DTO.Request.LoginRequest;
import com.splitwisepay.demo.DTO.Request.RegisterRequest;
import com.splitwisepay.demo.DTO.Response.AdminStatsResponse;
import com.splitwisepay.demo.DTO.Response.AuthResponse;
import com.splitwisepay.demo.DTO.Response.UserResponse;
import com.splitwisepay.demo.Entity.User;
import com.splitwisepay.demo.Repository.ExpenseRepository;
import com.splitwisepay.demo.Repository.GroupRepository;
import com.splitwisepay.demo.Repository.UserRepository;
import com.splitwisepay.demo.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    public AuthResponse adminRegister(RegisterRequest request) {


        // 1. Check if email already exists

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: "
                    + request.getEmail());
        }

        // 2. Build User entity — hash the password, never store plain text
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ADMIN)
                .isActive(true)
                .build();

        // 3. Save to DB
        User savedUser = userRepository.save(user);

        // 4. Generate JWT token
        String token = jwtService.generateToken(savedUser);

        // 5. Return token + user info (never return passwordHash)
        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role(savedUser.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        // 1. This throws BadCredentialsException if email/password wrong
        // AuthenticationManager internally calls CustomUserDetailsService
        // + BCrypt password comparison — you don't do it manually
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If we reach here, credentials are valid — load user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Generate fresh JWT token
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
    //-------------------------


    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;

    // ─────────────────────────────────────────────────────────────
    // GET ALL USERS
    // ─────────────────────────────────────────────────────────────
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    // DEACTIVATE USER
    // ─────────────────────────────────────────────────────────────
    public UserResponse deactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setIsActive(false);

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    // ─────────────────────────────────────────────────────────────
    // GET ADMIN STATS
    // ─────────────────────────────────────────────────────────────
    public AdminStatsResponse getStats() {

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalGroups(groupRepository.count())
                .totalExpenses(expenseRepository.count())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // ENTITY → DTO MAPPER
    // ─────────────────────────────────────────────────────────────
    private UserResponse mapToUserResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
}
