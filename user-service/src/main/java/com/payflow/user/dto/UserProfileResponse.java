package com.payflow.user.dto;

import com.payflow.common.model.enums.KycLevel;
import com.payflow.common.model.enums.UserRole;
import com.payflow.user.domain.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String phone,
        String status,
        UserRole role,
        KycLevel kycLevel,
        Instant createdAt
) {
    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getRole(),
                user.getKycLevel(),
                user.getCreatedAt()
        );
    }
}
