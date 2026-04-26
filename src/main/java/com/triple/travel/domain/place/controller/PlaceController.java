package com.triple.travel.domain.place.controller;

import com.triple.travel.common.dto.ApiResponse;
import com.triple.travel.domain.place.dto.PlaceDto;
import com.triple.travel.domain.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Place", description = "장소 검색 및 캐시")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "DB에 캐시된 장소 검색 (이름/주소/카테고리)")
    @GetMapping("/search")
    public ApiResponse<List<PlaceDto.Detail>> search(
        @Parameter(description = "이름/주소 키워드") @RequestParam(required = false) String keyword,
        @Parameter(description = "도시 필터") @RequestParam(required = false) String city,
        @Parameter(description = "카테고리 (RESTAURANT|CAFE|ATTRACTION|ACCOMMODATION|SHOPPING|OTHER)")
        @RequestParam(required = false) String category
    ) {
        return ApiResponse.ok(placeService.search(keyword, city, category));
    }

    @Operation(summary = "장소 상세 조회")
    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDto.Detail> getPlace(@PathVariable Long placeId) {
        return ApiResponse.ok(placeService.getById(placeId));
    }

    @Operation(summary = "Google Maps 검색 → DB 캐싱 (API 키 미설정 시 DB 검색으로 폴백)")
    @PostMapping("/import")
    public ApiResponse<List<PlaceDto.Detail>> importByQuery(@Valid @RequestBody PlaceDto.ImportRequest req) {
        return ApiResponse.ok(placeService.importByQuery(req.query()));
    }
}
