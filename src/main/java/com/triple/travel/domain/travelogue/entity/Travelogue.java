package com.triple.travel.domain.travelogue.entity;

import com.triple.travel.common.entity.BaseEntity;
import com.triple.travel.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "travelogues",
    indexes = {
        @Index(name = "idx_travelogues_user_status", columnList = "user_id, status"),
        // 공개 피드 커서 페이지네이션용
        @Index(name = "idx_travelogues_status_published", columnList = "status, published_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Travelogue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 원본 일정 참조 (느슨한 연결 - 일정 삭제 시에도 여행기는 유지)
    @Column(name = "source_itinerary_id")
    private Long sourceItineraryId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "cover_image_url", length = 512)
    private String coverImageUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "travel_start_date")
    private LocalDate travelStartDate;

    @Column(name = "travel_end_date")
    private LocalDate travelEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status;

    // 고빈도 읽기를 위한 비정규화 카운터
    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    @Column(name = "scrap_count", nullable = false)
    private Long scrapCount;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "travelogue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    private List<TravelogueDay> days = new ArrayList<>();

    @Builder
    private Travelogue(User user, Long sourceItineraryId, String title, String coverImageUrl,
                       String summary, String city, String country,
                       LocalDate travelStartDate, LocalDate travelEndDate) {
        this.user = user;
        this.sourceItineraryId = sourceItineraryId;
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.summary = summary;
        this.city = city;
        this.country = country;
        this.travelStartDate = travelStartDate;
        this.travelEndDate = travelEndDate;
        this.status = Status.DRAFT;
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.scrapCount = 0L;
    }

    public void publish() {
        this.status = Status.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void unpublish() {
        this.status = Status.DRAFT;
    }

    public void updateMeta(String title, String coverImageUrl, String summary) {
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.summary = summary;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void incrementScrapCount() {
        this.scrapCount++;
    }

    public void addDay(TravelogueDay day) {
        this.days.add(day);
    }

    public enum Status {
        DRAFT, PUBLISHED
    }
}
