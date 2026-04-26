package com.triple.travel.domain.itinerary.entity;

import com.triple.travel.common.entity.BaseEntity;
import com.triple.travel.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itinerary_slots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItinerarySlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_day_id", nullable = false)
    private ItineraryDay itineraryDay;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "slot_time")
    private LocalTime slotTime;

    // Swap 후 현재 선택된 장소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_category", length = 15)
    private TimeCategory timeCategory;

    @Column(name = "stay_duration_minutes")
    private Integer stayDurationMinutes;

    @Column(columnDefinition = "TEXT")
    private String memo;

    // AI가 제공한 대안 장소 목록 (최대 2개)
    // @BatchSize: alternatives를 한 번에 N개 슬롯분을 IN 쿼리로 로딩 → N+1 차단
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<ItinerarySlotAlternative> alternatives = new ArrayList<>();

    @Builder
    private ItinerarySlot(ItineraryDay itineraryDay, Integer orderIndex, LocalTime slotTime,
                          Place place, TimeCategory timeCategory, Integer stayDurationMinutes) {
        this.itineraryDay = itineraryDay;
        this.orderIndex = orderIndex;
        this.slotTime = slotTime;
        this.place = place;
        this.timeCategory = timeCategory;
        this.stayDurationMinutes = stayDurationMinutes;
    }

    public void update(LocalTime slotTime, Integer orderIndex, String memo, Integer stayDurationMinutes) {
        this.slotTime = slotTime;
        this.orderIndex = orderIndex;
        this.memo = memo;
        this.stayDurationMinutes = stayDurationMinutes;
    }

    // Swap 트랜잭션: slot의 현재 장소를 alternative의 장소로 교체
    public void swapPlace(Place newPlace) {
        this.place = newPlace;
    }

    public enum TimeCategory {
        BREAKFAST, MORNING, LUNCH, AFTERNOON, DINNER, NIGHT
    }
}
