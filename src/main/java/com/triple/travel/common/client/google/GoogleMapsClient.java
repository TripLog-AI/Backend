package com.triple.travel.common.client.google;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Google Places API (New v1) 클라이언트.
 * api-key가 비어 있으면 호출하지 않고 빈 결과를 반환 (개발/시연 fallback).
 *
 * TODO: 실제 Places searchText / placeDetails 엔드포인트 통합.
 *  - POST https://places.googleapis.com/v1/places:searchText
 *  - GET  https://places.googleapis.com/v1/places/{placeId}
 *  - 헤더: X-Goog-Api-Key, X-Goog-FieldMask
 */
@Slf4j
@Component
public class GoogleMapsClient {

    private final String apiKey;
    private final RestClient restClient;

    public GoogleMapsClient(@Value("${app.google.maps.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
            .baseUrl("https://places.googleapis.com/v1")
            .build();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GoogleMapsClient: api-key not set — external calls disabled, DB cache only");
        }
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * 텍스트 검색. 키 없으면 빈 목록 반환.
     */
    public List<GoogleMapsDto.PlaceSearchResult> searchText(String query) {
        if (!isEnabled()) return List.of();
        // TODO: 실제 Places searchText API 호출
        log.debug("GoogleMapsClient.searchText (stubbed): query={}", query);
        return List.of();
    }
}
