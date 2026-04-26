package com.triple.travel.domain.youtube.controller;

import com.triple.travel.common.dto.ApiResponse;
import com.triple.travel.common.security.AuthPrincipal;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.youtube.dto.YoutubeCourseDto;
import com.triple.travel.domain.youtube.service.YoutubeCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "YouTube Courses", description = "홈 화면 유튜버 여행 코스 API")
@RestController
@RequestMapping("/api/v1/youtube-courses")
@RequiredArgsConstructor
public class YoutubeCourseController {

    private final YoutubeCourseService youtubeCourseService;

    @Operation(summary = "홈 — 추천 유튜버 코스 목록 (커서 페이지네이션)")
    @GetMapping
    public ApiResponse<List<YoutubeCourseDto.Item>> getFeaturedCourses(
        @Parameter(description = "도시 키워드 필터") @RequestParam(required = false) String city,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(youtubeCourseService.getFeaturedCourses(city, cursor, size));
    }

    @Operation(summary = "유튜버 코스 상세")
    @GetMapping("/{courseId}")
    public ApiResponse<YoutubeCourseDto.Detail> getCourseDetail(@PathVariable Long courseId) {
        return ApiResponse.ok(youtubeCourseService.getCourseDetail(courseId));
    }

    @Operation(summary = "유튜버 코스를 내 여행에 저장 (현재는 메타 정보만 복사한 빈 일정 생성)")
    @PostMapping("/{courseId}/save")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItineraryResponse.Created> saveCourse(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long courseId
    ) {
        return ApiResponse.ok(youtubeCourseService.saveCourse(user.userId(), courseId));
    }
}
