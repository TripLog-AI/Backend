package com.triple.travel.domain.place.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlaceDto {

    public record Detail(
        Long id,
        String googlePlaceId,
        String name,
        String nameLocal,
        String address,
        String category,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal googleRating,
        String thumbnailUrl,
        LocalDateTime cachedAt
    ) {}

    public record ImportRequest(
        @NotBlank String query
    ) {}
}
