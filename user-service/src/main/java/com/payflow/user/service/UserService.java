package com.payflow.user.service;

import com.payflow.common.model.exception.PayFlowException;
import com.payflow.user.domain.entity.User;
import com.payflow.user.domain.repository.UserRepository;
import com.payflow.user.dto.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PayFlowException("USER_NOT_FOUND", "User with ID " + userId + " not found", 404));
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public UserProfileResponse updateUserStatus(UUID userId, String newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PayFlowException("USER_NOT_FOUND", "User with ID " + userId + " not found", 404));
        user.setStatus(newStatus.toUpperCase());
        user = userRepository.save(user);
        return UserProfileResponse.fromEntity(user);
    }
}
