package com.triple.travel.domain.itinerary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class ItineraryRequest {

    /**
     * 빈 일정 직접 생성
     */
    public record CreateManual(
        @NotBlank String title,
        String city,
        String country,
        LocalDate startDate,
        LocalDate endDate
    ) {}

    /**
     * AI 일정 생성 폼 입력
     */
    public record GenerateByAi(
        @NotBlank String city,
        @NotBlank String country,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String companionType,    // SOLO | COUPLE | FAMILY | FRIENDS
        List<String> themes,     // ["FOOD", "CULTURE", ...]
        String pace              // RELAXED | NORMAL | PACKED
    ) {}

    /**
     * YouTube URL 파싱 요청
     */
    public record ParseYoutube(
        @NotBlank String youtubeUrl
    ) {}

    /**
     * 일정 메타 정보 수정
     */
    public record UpdateMeta(
        String title,
        String city,
        String country,
        LocalDate startDate,
        LocalDate endDate
    ) {}

    /**
     * Day 추가
     */
    public record AddDay(
        @NotNull Integer dayNumber,
        LocalDate date
    ) {}

    /**
     * Day 수정
     */
    public record UpdateDay(
        LocalDate date,
        String memo
    ) {}

    /**
     * 슬롯 추가
     */
    public record AddSlot(
        @NotNull Long placeId,
        @NotNull Integer orderIndex,
        String slotTime,          // "12:00"
        String timeCategory,      // BREAKFAST | MORNING | LUNCH | AFTERNOON | DINNER | NIGHT
        Integer stayDurationMinutes
    ) {}

    /**
     * 슬롯 수정
     */
    public record UpdateSlot(
        String slotTime,
        Integer orderIndex,
        String memo,
        Integer stayDurationMinutes
    ) {}

    /**
     * 대안 장소로 교체
     */
    public record SwapPlace(
        @NotNull Long alternativeId
    ) {}

    /**
     * 슬롯 순서 일괄 변경
     */
    public record ReorderSlots(
        @NotNull List<SlotOrder> orders
    ) {
        public record SlotOrder(Long slotId, Integer orderIndex) {}
    }
}
