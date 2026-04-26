package com.triple.travel.domain.travelogue.entity;

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
    name = "travelogue_days",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_travelogue_days_travelogue_day",
        columnNames = {"travelogue_id", "day_number"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelogueDay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travelogue_id", nullable = false)
    private Travelogue travelogue;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    private LocalDate date;

    @OneToMany(mappedBy = "travelogueDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<TravelogueBlock> blocks = new ArrayList<>();

    @Builder
    private TravelogueDay(Travelogue travelogue, Integer dayNumber, LocalDate date) {
        this.travelogue = travelogue;
        this.dayNumber = dayNumber;
        this.date = date;
    }

    public void addBlock(TravelogueBlock block) {
        this.blocks.add(block);
    }
}
