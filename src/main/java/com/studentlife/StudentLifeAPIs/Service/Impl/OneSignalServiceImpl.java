package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Service.OneSignalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OneSignalServiceImpl implements OneSignalService {

    @Value("${onesignal.app-id}")
    private String appId;

    @Value("${onesignal.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Override
    public void sendPushToUser(String playerId, String title, String message, Long referenceId, String link) {
        if (playerId == null || playerId.isBlank()) return;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Deep-linking payload so click-through works from push too. OneSignal "data"
            // values must be strings; the client mirrors the in-app resolveDestination() logic.
            Map<String, Object> data = new HashMap<>();
            if (referenceId != null) {
                data.put("referenceId", String.valueOf(referenceId));
            }
            if (link != null && !link.isBlank()) {
                data.put("link", link);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("app_id", appId);
            body.put("include_player_ids", List.of(playerId));
            body.put("headings", Map.of("en", title));
            body.put("contents", Map.of("en", message));
            if (!data.isEmpty()) {
                body.put("data", data);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(
                    "https://onesignal.com/api/v1/notifications",
                    request,
                    String.class
            );

            log.info("[OneSignal] Push sent to playerId={}", playerId);
        } catch (Exception e) {
            // Never let OneSignal failure crash the chat flow
            log.warn("[OneSignal] Failed to send push to playerId={} — {}", playerId, e.getMessage());
        }
    }
}
