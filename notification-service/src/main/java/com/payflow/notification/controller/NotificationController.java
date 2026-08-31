package com.payflow.notification.controller;

import com.payflow.common.model.response.ApiResponse;
import com.payflow.notification.dto.NotificationResponse;
import com.payflow.notification.dto.SendNotificationRequest;
import com.payflow.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request
    ) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationHistory(
            @PathVariable UUID userId
    ) {
        List<NotificationResponse> history = notificationService.getNotificationHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
