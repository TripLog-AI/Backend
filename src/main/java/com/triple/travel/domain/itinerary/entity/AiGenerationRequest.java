package com.triple.travel.domain.itinerary.entity;

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
    name = "ai_generation_requests",
    indexes = {
        @Index(name = "idx_ai_req_user_status", columnList = "user_id, status"),
        // 배치 처리 시 PENDING/PROCESSING 건을 created_at 순서로 조회
        @Index(name = "idx_ai_req_status_created", columnList = "status, created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiGenerationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private RequestType requestType;

    // AI 폼 입력값 또는 youtube_url을 JSON으로 직렬화하여 저장
    @Column(name = "input_data", nullable = false, columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "result_itinerary_id")
    private Long resultItineraryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private AiGenerationRequest(User user, RequestType requestType, String inputData) {
        this.user = user;
        this.requestType = requestType;
        this.inputData = inputData;
        this.status = Status.PENDING;
    }

    public void startProcessing() {
        this.status = Status.PROCESSING;
    }

    public void complete(Long itineraryId) {
        this.resultItineraryId = itineraryId;
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = Status.FAILED;
        this.completedAt = LocalDateTime.now();
    }

    public enum RequestType {
        AI_FORM, YOUTUBE_PARSE
    }

    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
