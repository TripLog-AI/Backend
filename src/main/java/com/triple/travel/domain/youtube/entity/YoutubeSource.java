package com.triple.travel.domain.youtube.entity;

import com.triple.travel.common.entity.BaseEntity;
import com.triple.travel.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "youtube_sources",
    indexes = {
        @Index(name = "idx_youtube_video_id", columnList = "video_id"),
        @Index(name = "idx_youtube_featured_status", columnList = "is_featured, parse_status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YoutubeSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false, unique = true, length = 50)
    private String videoId;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(length = 512)
    private String title;

    @Column(name = "channel_name", length = 255)
    private String channelName;

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false, length = 20)
    private ParseStatus parseStatus;

    // 홈 화면 노출용 시스템 시드 여부
    @Column(name = "is_featured", nullable = false)
    private boolean featured;

    // null이면 시스템 등록 소스
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    @Builder
    private YoutubeSource(String videoId, String url, String title, String channelName,
                          String thumbnailUrl, boolean featured, User submittedBy) {
        this.videoId = videoId;
        this.url = url;
        this.title = title;
        this.channelName = channelName;
        this.thumbnailUrl = thumbnailUrl;
        this.parseStatus = ParseStatus.PENDING;
        this.featured = featured;
        this.submittedBy = submittedBy;
    }

    public void startProcessing() {
        this.parseStatus = ParseStatus.PROCESSING;
    }

    public void markCompleted() {
        this.parseStatus = ParseStatus.COMPLETED;
        this.parsedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.parseStatus = ParseStatus.FAILED;
    }

    public enum ParseStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
