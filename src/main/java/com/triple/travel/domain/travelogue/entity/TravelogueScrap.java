package com.triple.travel.domain.travelogue.entity;

import com.triple.travel.domain.itinerary.entity.Itinerary;
import com.triple.travel.domain.user.entity.User;
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
    name = "travelogue_scraps",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_travelogue_scraps_user_travelogue",
        columnNames = {"user_id", "travelogue_id"}
    )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelogueScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travelogue_id", nullable = false)
    private Travelogue travelogue;

    // 스크랩으로 생성된 내 여행 일정 - 역추적 및 삭제 연계용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_itinerary_id", nullable = false)
    private Itinerary createdItinerary;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private TravelogueScrap(User user, Travelogue travelogue, Itinerary createdItinerary) {
        this.user = user;
        this.travelogue = travelogue;
        this.createdItinerary = createdItinerary;
    }
}
