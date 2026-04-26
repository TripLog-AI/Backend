package com.triple.travel.domain.youtube.dto;

import com.triple.travel.domain.itinerary.dto.ItineraryResponse;

public class YoutubeCourseDto {

    /** 홈 피드 카드용 요약 */
    public record Item(
        Long id,
        String videoId,
        String title,
        String channelName,
        String thumbnailUrl,
        String youtubeUrl
    ) {}

    /** 코스 상세 — 시드 일정 미리보기 포함 (없으면 null) */
    public record Detail(
        Long id,
        String videoId,
        String title,
        String channelName,
        String thumbnailUrl,
        String youtubeUrl,
        ItineraryResponse.Detail previewItinerary
    ) {}
}
