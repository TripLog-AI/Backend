package com.triple.travel.domain.travelogue.entity;

import com.triple.travel.common.entity.BaseEntity;
import com.triple.travel.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 여행기의 블로그 형태 콘텐츠 블록.
 * TEXT / IMAGE / PLACE 세 가지 타입으로 구성되며,
 * 발행 시점에 Itinerary의 슬롯 정보를 PLACE 블록으로 스냅샷 복사함.
 */
@Entity
@Table(name = "travelogue_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelogueBlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travelogue_day_id", nullable = false)
    private TravelogueDay travelogueDay;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, length = 10)
    private BlockType blockType;

    // TEXT 블록 본문
    @Column(columnDefinition = "TEXT")
    private String content;

    // IMAGE 블록 URL
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    // PLACE 블록 - 발행 시 snapshot된 장소 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "place_visit_time")
    private LocalTime placeVisitTime;

    @Column(name = "place_memo", columnDefinition = "TEXT")
    private String placeMemo;

    @Builder
    private TravelogueBlock(TravelogueDay travelogueDay, Integer orderIndex, BlockType blockType,
                             String content, String imageUrl, Place place,
                             LocalTime placeVisitTime, String placeMemo) {
        this.travelogueDay = travelogueDay;
        this.orderIndex = orderIndex;
        this.blockType = blockType;
        this.content = content;
        this.imageUrl = imageUrl;
        this.place = place;
        this.placeVisitTime = placeVisitTime;
        this.placeMemo = placeMemo;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateOrder(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public enum BlockType {
        TEXT, IMAGE, PLACE
    }
}
