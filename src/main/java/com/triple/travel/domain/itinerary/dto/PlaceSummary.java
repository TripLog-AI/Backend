package com.triple.travel.domain.itinerary.dto;

import java.math.BigDecimal;

/**
 * 장소 요약 정보 - 슬롯과 대안 장소 응답에 공통으로 사용
 */
public record PlaceSummary(
    Long id,
    String name,
    String address,
    String category,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal googleRating,
    String thumbnailUrl
) {}
