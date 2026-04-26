package com.triple.travel.domain.itinerary.entity;

import com.triple.travel.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "itinerary_days",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_itinerary_days_itinerary_day",
        columnNames = {"itinerary_id", "day_number"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItineraryDay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    // 여행 시작일 미설정 시 null 허용
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @OneToMany(mappedBy = "itineraryDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<ItinerarySlot> slots = new ArrayList<>();

    @Builder
    private ItineraryDay(Itinerary itinerary, Integer dayNumber, LocalDate date) {
        this.itinerary = itinerary;
        this.dayNumber = dayNumber;
        this.date = date;
    }

    public void update(LocalDate date, String memo) {
        this.date = date;
        this.memo = memo;
    }

    public void addSlot(ItinerarySlot slot) {
        this.slots.add(slot);
    }
}
