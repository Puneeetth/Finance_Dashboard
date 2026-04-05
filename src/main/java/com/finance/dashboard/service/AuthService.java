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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
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

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

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
