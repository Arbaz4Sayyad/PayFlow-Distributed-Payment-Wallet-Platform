package com.payflow.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.security.JwtTokenProvider;
import com.payflow.notification.domain.enums.NotificationChannel;
import com.payflow.notification.domain.enums.NotificationStatus;
import com.payflow.notification.dto.NotificationResponse;
import com.payflow.notification.dto.SendNotificationRequest;
import com.payflow.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("NotificationController WebMvc Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/notifications/send - Should send notification and return 201 Created")
    void shouldSendNotificationSuccessfully() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        SendNotificationRequest request = new SendNotificationRequest(
                eventId, userId, NotificationChannel.EMAIL, "user@test.com", "Test Subject", "Test Body"
        );

        NotificationResponse response = new NotificationResponse(
                UUID.randomUUID(), eventId, userId, NotificationChannel.EMAIL,
                "user@test.com", "Test Subject", "Test Body", NotificationStatus.SENT,
                Instant.now(), Instant.now()
        );

        when(notificationService.sendNotification(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channel").value("EMAIL"))
                .andExpect(jsonPath("$.data.status").value("SENT"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{userId} - Should return notification history")
    void shouldGetNotificationHistory() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationResponse response = new NotificationResponse(
                UUID.randomUUID(), UUID.randomUUID(), userId, NotificationChannel.SMS,
                "+919876543210", null, "Your OTP is 123456", NotificationStatus.SENT,
                Instant.now(), Instant.now()
        );

        when(notificationService.getNotificationHistory(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/notifications/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].channel").value("SMS"))
                .andExpect(jsonPath("$.data[0].status").value("SENT"));
    }
}
