package com.triple.travel.domain.youtube.controller;

import com.triple.travel.common.dto.ApiResponse;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.itinerary.dto.PlaceSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "YouTube Courses", description = "홈 화면 유튜버 여행 코스 API")
@RestController
@RequestMapping("/api/v1/youtube-courses")
public class YoutubeCourseController {

    public record YoutubeCourseItem(
        Long id,
        String videoId,
        String title,
        String channelName,
        String thumbnailUrl,
        String city,
        String country,
        Integer totalDays
    ) {}

    public record YoutubeCourseDetail(
        Long id,
        String videoId,
        String title,
        String channelName,
        String thumbnailUrl,
        String city,
        String country,
        ItineraryResponse.Detail previewItinerary
    ) {}

    private PlaceSummary mockPlace(Long id, String name, String address, String category,
                                    double lat, double lng, double rating) {
        return new PlaceSummary(id, name, address, category,
            BigDecimal.valueOf(lat), BigDecimal.valueOf(lng),
            BigDecimal.valueOf(rating), "https://images.unsplash.com/photo-mock-" + id);
    }

    @Operation(summary = "홈 화면 - 추천 유튜버 코스 목록")
    @GetMapping
    public ApiResponse<List<YoutubeCourseItem>> getFeaturedCourses(
        @RequestParam(required = false) String city,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10") int size
    ) {
        List<YoutubeCourseItem> mock = List.of(
            new YoutubeCourseItem(1L, "dQw4w9WgXcQ",
                "도쿄 2박 3일 완벽 가이드 | 현지인이 추천하는 숨은 맛집",
                "여행하는 삶", "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
                "Tokyo", "Japan", 3),
            new YoutubeCourseItem(2L, "xvFZjo5PgG0",
                "파리 혼자 여행 4박 5일 Vlog | 에펠탑, 루브르, 몽마르트",
                "솔로트래블러", "https://img.youtube.com/vi/xvFZjo5PgG0/maxresdefault.jpg",
                "Paris", "France", 5),
            new YoutubeCourseItem(3L, "C0DPdy98e4c",
                "바르셀로나 3일 루트 | 가우디 건축 투어 완전정복",
                "건축여행자", "https://img.youtube.com/vi/C0DPdy98e4c/maxresdefault.jpg",
                "Barcelona", "Spain", 3)
        );
        return ApiResponse.ok(mock);
    }

    @Operation(summary = "유튜버 코스 상세 + 미리보기 일정")
    @GetMapping("/{courseId}")
    public ApiResponse<YoutubeCourseDetail> getCourseDetail(@PathVariable Long courseId) {
        PlaceSummary shinjuku  = mockPlace(20L, "신주쿠 교엔",     "11 Naitomachi, Shinjuku",  "ATTRACTION", 35.6851, 139.7101, 4.6);
        PlaceSummary ichiran   = mockPlace(21L, "이치란 라멘 신주쿠", "3-34-11 Shinjuku",        "RESTAURANT", 35.6897, 139.7006, 4.5);
        PlaceSummary shibuya   = mockPlace(22L, "시부야 스크램블",   "2-1 Dogenzaka, Shibuya",  "ATTRACTION", 35.6595, 139.7004, 4.7);
        PlaceSummary tsukiji   = mockPlace(23L, "츠키지 시장",       "5-2-1 Tsukiji, Chuo",     "ATTRACTION", 35.6654, 139.7707, 4.4);

        ItineraryResponse.SlotDetail slot1 = new ItineraryResponse.SlotDetail(
            301L, 1, LocalTime.of(9, 0), "MORNING", 120, null, shinjuku, List.of());
        ItineraryResponse.SlotDetail slot2 = new ItineraryResponse.SlotDetail(
            302L, 2, LocalTime.of(12, 0), "LUNCH", 60, "혼밥 가능", ichiran, List.of());
        ItineraryResponse.SlotDetail slot3 = new ItineraryResponse.SlotDetail(
            303L, 3, LocalTime.of(18, 0), "AFTERNOON", 90, null, shibuya, List.of());
        ItineraryResponse.SlotDetail slot4 = new ItineraryResponse.SlotDetail(
            304L, 4, LocalTime.of(7, 0), "BREAKFAST", 90, "새벽 경매 구경", tsukiji, List.of());

        ItineraryResponse.DayDetail day1 = new ItineraryResponse.DayDetail(
            30L, 1, LocalDate.of(2024, 10, 1), null, List.of(slot1, slot2, slot3));
        ItineraryResponse.DayDetail day2 = new ItineraryResponse.DayDetail(
            31L, 2, LocalDate.of(2024, 10, 2), null, List.of(slot4));

        ItineraryResponse.Detail preview = new ItineraryResponse.Detail(
            courseId, "도쿄 2박 3일 완벽 가이드", "Tokyo", "Japan",
            LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 3),
            null, List.of("FOOD", "CULTURE"), null,
            "YOUTUBE_SEED", "ACTIVE",
            List.of(day1, day2),
            LocalDateTime.of(2024, 6, 1, 0, 0),
            LocalDateTime.of(2024, 6, 1, 0, 0)
        );

        YoutubeCourseDetail detail = new YoutubeCourseDetail(
            courseId, "dQw4w9WgXcQ",
            "도쿄 2박 3일 완벽 가이드 | 현지인이 추천하는 숨은 맛집",
            "여행하는 삶", "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
            "Tokyo", "Japan", preview
        );
        return ApiResponse.ok(detail);
    }

    @Operation(summary = "유튜버 코스를 내 여행에 저장 (딥카피 → 내 계정 일정 생성)")
    @PostMapping("/{courseId}/save")
    public ApiResponse<ItineraryResponse.Created> saveCourse(@PathVariable Long courseId) {
        // Mock: 새로 생성된 내 일정 ID 반환
        return ApiResponse.ok(new ItineraryResponse.Created(99L));
    }
}
