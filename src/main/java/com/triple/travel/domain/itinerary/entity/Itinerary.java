package com.triple.travel.domain.itinerary.entity;

import com.triple.travel.common.entity.BaseEntity;
import com.triple.travel.domain.user.entity.User;
import com.triple.travel.domain.youtube.entity.YoutubeSource;
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
    name = "itineraries",
    indexes = {
        @Index(name = "idx_itineraries_user_status", columnList = "user_id, status"),
        @Index(name = "idx_itineraries_source_type", columnList = "source_type")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Itinerary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "companion_type", length = 20)
    private CompanionType companionType;

    // JSON 배열을 문자열로 저장 e.g. '["FOOD","CULTURE"]'
    @Column(length = 255)
    private String themes;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Pace pace;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youtube_source_id")
    private YoutubeSource youtubeSource;

    // Travelogue 삭제 시 영향을 주지 않기 위해 ID만 보관
    @Column(name = "origin_travelogue_id")
    private Long originTravelogueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    // days 조회 시 항상 정렬된 상태로 반환
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    private List<ItineraryDay> days = new ArrayList<>();

    @Builder
    private Itinerary(User user, String title, String city, String country,
                      LocalDate startDate, LocalDate endDate, CompanionType companionType,
                      String themes, Pace pace, SourceType sourceType,
                      YoutubeSource youtubeSource, Long originTravelogueId) {
        this.user = user;
        this.title = title;
        this.city = city;
        this.country = country;
        this.startDate = startDate;
        this.endDate = endDate;
        this.companionType = companionType;
        this.themes = themes;
        this.pace = pace;
        this.sourceType = sourceType;
        this.youtubeSource = youtubeSource;
        this.originTravelogueId = originTravelogueId;
        this.status = Status.DRAFT;
    }

    public void updateMeta(String title, String city, String country,
                           LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.city = city;
        this.country = country;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void archive() {
        this.status = Status.ARCHIVED;
    }

    public void addDay(ItineraryDay day) {
        this.days.add(day);
    }

    public enum CompanionType {
        SOLO, COUPLE, FAMILY, FRIENDS
    }

    public enum Pace {
        RELAXED, NORMAL, PACKED
    }

    public enum SourceType {
        MANUAL, AI_GENERATED, YOUTUBE_PARSED, YOUTUBE_SEED, SCRAPED
    }

    public enum Status {
        DRAFT, ACTIVE, ARCHIVED
    }
}
