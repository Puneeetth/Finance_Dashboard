package com.finance.dashboard.service;

import com.finance.dashboard.Transformer.UserTransformer;
import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.RegisterRequest;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.enums.UserStatus;
import com.finance.dashboard.models.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.CustomUserDetails;
import com.finance.dashboard.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserTransformer userTransformer;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsernameIncludingDeleted(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken or scheduled for deletion. Please login to reactivate.");
        }
        if (userRepository.findByEmailIncludingDeleted(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use or scheduled for deletion. Please login to reactivate.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .deleted(false)
                .build();

        userRepository.save(user);

        String jwtToken = jwtUtil.generateToken(new CustomUserDetails(user));
        return AuthResponse.builder()
                .token(jwtToken)
                .user(userTransformer.toResponse(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsernameIncludingDeleted(request.getUsername())
                .orElseThrow();

        // Reactivate soft-deleted user
        if (user.isDeleted()) {
            user.setDeleted(false);
            user.setDeletedAt(null);
            userRepository.save(user);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User account is inactive");
        }

        String jwtToken = jwtUtil.generateToken(new CustomUserDetails(user));
        return AuthResponse.builder()
                .token(jwtToken)
                .user(userTransformer.toResponse(user))
                .build();
    }
}
