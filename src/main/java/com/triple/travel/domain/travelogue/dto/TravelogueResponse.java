package com.triple.travel.domain.travelogue.dto;

import com.triple.travel.domain.itinerary.dto.PlaceSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TravelogueResponse {

    public record Created(Long travelogueId) {}

    /**
     * 피드 목록용 요약
     */
    public record Summary(
        Long id,
        String title,
        String coverImageUrl,
        String summary,
        String city,
        String country,
        LocalDate travelStartDate,
        LocalDate travelEndDate,
        AuthorInfo author,
        long likeCount,
        long scrapCount,
        long viewCount,
        String status,
        LocalDateTime publishedAt
    ) {}

    /**
     * 여행기 상세 - days + blocks 전체
     */
    public record Detail(
        Long id,
        String title,
        String coverImageUrl,
        String summary,
        String city,
        String country,
        LocalDate travelStartDate,
        LocalDate travelEndDate,
        AuthorInfo author,
        long likeCount,
        long scrapCount,
        long viewCount,
        boolean likedByMe,
        boolean scrappedByMe,
        String status,
        LocalDateTime publishedAt,
        List<DayDetail> days
    ) {}

    public record AuthorInfo(
        Long userId,
        String nickname,
        String profileImageUrl
    ) {}

    public record DayDetail(
        Long dayId,
        Integer dayNumber,
        LocalDate date,
        List<BlockDetail> blocks
    ) {}

    public record BlockDetail(
        Long blockId,
        Integer orderIndex,
        String blockType,
        String content,
        String imageUrl,
        PlaceSummary place,
        LocalTime placeVisitTime,
        String placeMemo
    ) {}
}
