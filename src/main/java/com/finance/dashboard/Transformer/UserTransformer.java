package com.finance.dashboard.Transformer;

import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserTransformer {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
