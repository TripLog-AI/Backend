package com.triple.travel.domain.travelogue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TravelogueRequest {

    /**
     * 발행: Itinerary를 Travelogue로 스냅샷 변환.
     * title 미입력 시 원본 Itinerary 제목 사용.
     */
    public record Publish(
        @NotNull Long itineraryId,
        String title,
        String coverImageUrl,
        String summary
    ) {}

    /**
     * 여행기 메타 수정
     */
    public record UpdateMeta(
        @NotBlank String title,
        String coverImageUrl,
        String summary
    ) {}

    /**
     * 콘텐츠 블록 추가 (TEXT / IMAGE / PLACE)
     */
    public record AddBlock(
        @NotNull Integer orderIndex,
        @NotNull String blockType,
        String content,
        String imageUrl,
        Long placeId,
        String placeVisitTime,
        String placeMemo
    ) {}

    /**
     * 블록 순서 일괄 변경
     */
    public record ReorderBlocks(
        @NotNull java.util.List<BlockOrder> orders
    ) {
        public record BlockOrder(Long blockId, Integer orderIndex) {}
    }
}
