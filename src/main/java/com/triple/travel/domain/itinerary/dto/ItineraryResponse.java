package com.triple.travel.domain.itinerary.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ItineraryResponse {

    /**
     * 목록 조회용 요약
     */
    public record Summary(
        Long id,
        String title,
        String city,
        String country,
        LocalDate startDate,
        LocalDate endDate,
        String sourceType,
        String status,
        LocalDateTime createdAt
    ) {}

    /**
     * 상세 조회 - days + slots + alternatives 전체 포함
     */
    public record Detail(
        Long id,
        String title,
        String city,
        String country,
        LocalDate startDate,
        LocalDate endDate,
        String companionType,
        List<String> themes,
        String pace,
        String sourceType,
        String status,
        List<DayDetail> days,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record DayDetail(
        Long dayId,
        Integer dayNumber,
        LocalDate date,
        String memo,
        List<SlotDetail> slots
    ) {}

    public record SlotDetail(
        Long slotId,
        Integer orderIndex,
        LocalTime slotTime,
        String timeCategory,
        Integer stayDurationMinutes,
        String memo,
        PlaceSummary place,
        List<AlternativeDetail> alternatives  // AI 생성 시에만 존재
    ) {}

    public record AlternativeDetail(
        Long alternativeId,
        Integer orderIndex,
        PlaceSummary place
    ) {}

    /**
     * 비동기 요청 상태 조회
     */
    public record JobStatus(
        Long requestId,
        String status,          // PENDING | PROCESSING | COMPLETED | FAILED
        Long itineraryId,       // COMPLETED 시에만 non-null
        String errorMessage     // FAILED 시에만 non-null
    ) {}

    /**
     * 일정 생성/수정 완료 시 단순 ID 반환
     */
    public record Created(
        Long itineraryId
    ) {}
}
