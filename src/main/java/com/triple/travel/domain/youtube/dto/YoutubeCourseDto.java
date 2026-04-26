package com.triple.travel.domain.youtube.dto;

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

    /** 코스 상세 (시드 일정 미연동 단계) */
    public record Detail(
        Long id,
        String videoId,
        String title,
        String channelName,
        String thumbnailUrl,
        String youtubeUrl
    ) {}
}
