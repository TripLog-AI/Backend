package com.triple.travel.common.client.google;

import java.math.BigDecimal;

public class GoogleMapsDto {

    /** searchText 응답 단일 항목 (Places API New v1) */
    public record PlaceSearchResult(
        String googlePlaceId,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal rating,
        String thumbnailUrl
    ) {}
}
