package com.triple.travel.common.client.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * BE ↔ AI FastAPI 인터페이스 DTO. Docs/API_CONTRACT.md 섹션 2와 일치.
 */
public class AiServiceDto {

    // ── Request ────────────────────────────────────────────

    public record GenerateRequest(
        String city,
        String country,
        LocalDate startDate,
        LocalDate endDate,
        String companionType,
        List<String> themes,
        String pace
    ) {}

    public record YoutubeRequest(
        String youtubeUrl,
        List<String> targetLanguages
    ) {}

    // ── Response (generate / youtube 공통 구조) ────────────

    public record ItineraryPayload(
        String city,
        String country,
        List<DayDto> days
    ) {}

    public record DayDto(
        Integer dayNumber,
        List<SlotDto> slots
    ) {}

    public record SlotDto(
        String slotTime,
        String timeCategory,
        Integer stayDurationMinutes,
        PlaceDto place,
        List<AlternativeDto> alternatives
    ) {}

    public record AlternativeDto(
        PlaceDto place
    ) {}

    public record PlaceDto(
        String name,
        String nameLocal,
        String address,
        String category,
        BigDecimal latitude,
        BigDecimal longitude,
        String googlePlaceQuery
    ) {}
}
