package com.triple.travel.domain.itinerary.entity;

import com.triple.travel.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "itinerary_slot_alternatives",
    uniqueConstraints = {
        // 같은 슬롯 내 동일 순서 중복 방지
        @UniqueConstraint(name = "uk_alt_slot_order",
            columnNames = {"slot_id", "order_index"}),
        // 같은 슬롯에 동일 장소 중복 방지
        @UniqueConstraint(name = "uk_alt_slot_place",
            columnNames = {"slot_id", "place_id"})
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItinerarySlotAlternative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private ItinerarySlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // 1 or 2
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private ItinerarySlotAlternative(ItinerarySlot slot, Place place, Integer orderIndex) {
        this.slot = slot;
        this.place = place;
        this.orderIndex = orderIndex;
    }

    // Swap 완료 후 이 대안의 장소를 구 장소로 교체
    public void updatePlace(Place newPlace) {
        this.place = newPlace;
    }
}
