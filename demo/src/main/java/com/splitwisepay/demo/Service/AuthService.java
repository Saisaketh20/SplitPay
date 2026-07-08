package com.splitwisepay.demo.Service;

import com.splitwisepay.demo.DTO.Request.LoginRequest;
import com.splitwisepay.demo.DTO.Request.RegisterRequest;
import com.splitwisepay.demo.DTO.Response.AuthResponse;
import com.splitwisepay.demo.Entity.User;
import com.splitwisepay.demo.Repository.UserRepository;
import com.splitwisepay.demo.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Service/AuthService.java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

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
                .role(User.Role.USER)
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
}