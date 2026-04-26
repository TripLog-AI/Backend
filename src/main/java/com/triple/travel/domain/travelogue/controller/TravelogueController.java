package com.triple.travel.domain.travelogue.controller;

import com.triple.travel.common.dto.ApiResponse;
import com.triple.travel.common.security.AuthPrincipal;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.travelogue.dto.TravelogueRequest;
import com.triple.travel.domain.travelogue.dto.TravelogueResponse;
import com.triple.travel.domain.travelogue.service.TravelogueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Travelogue", description = "여행기 발행/피드/스크랩/좋아요")
@RestController
@RequestMapping("/api/v1/travelogues")
@RequiredArgsConstructor
public class TravelogueController {

    private final TravelogueService travelogueService;

    // ── 피드 / 상세 (GET은 비로그인 허용) ──────────

    @Operation(summary = "공개 여행기 피드 (도시 필터 + 커서 페이지네이션)")
    @GetMapping
    public ApiResponse<List<TravelogueResponse.Summary>> getFeed(
        @Parameter(description = "도시 필터") @RequestParam(required = false) String city,
        @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(travelogueService.getFeed(city, cursor, size));
    }

    @Operation(summary = "여행기 상세 (조회수 자동 증가)")
    @GetMapping("/{travelogueId}")
    public ApiResponse<TravelogueResponse.Detail> getTravelogue(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId
    ) {
        Long userId = user != null ? user.userId() : null;
        return ApiResponse.ok(travelogueService.getTravelogue(travelogueId, userId));
    }

    @Operation(summary = "내가 쓴 여행기 목록")
    @GetMapping("/me")
    public ApiResponse<List<TravelogueResponse.Summary>> getMyTravelogues(
        @AuthenticationPrincipal AuthPrincipal user
    ) {
        return ApiResponse.ok(travelogueService.getMyTravelogues(user.userId()));
    }

    // ── 발행 / 메타 / 삭제 ────────────────────────

    @Operation(summary = "여행기 발행 — Itinerary를 스냅샷으로 변환")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TravelogueResponse.Created> publish(
        @AuthenticationPrincipal AuthPrincipal user,
        @Valid @RequestBody TravelogueRequest.Publish request
    ) {
        return ApiResponse.ok(travelogueService.publish(user.userId(), request));
    }

    @Operation(summary = "여행기 메타 수정 (제목, 표지, 요약)")
    @PatchMapping("/{travelogueId}")
    public ApiResponse<Void> updateMeta(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId,
        @Valid @RequestBody TravelogueRequest.UpdateMeta request
    ) {
        travelogueService.updateMeta(travelogueId, user.userId(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "여행기 비공개 전환 (공개 → DRAFT)")
    @PostMapping("/{travelogueId}/unpublish")
    public ApiResponse<Void> unpublish(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId
    ) {
        travelogueService.unpublish(travelogueId, user.userId());
        return ApiResponse.ok();
    }

    @Operation(summary = "여행기 삭제")
    @DeleteMapping("/{travelogueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTravelogue(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId
    ) {
        travelogueService.deleteTravelogue(travelogueId, user.userId());
    }

    // ── 콘텐츠 블록 ──────────────────────────────

    @Operation(summary = "콘텐츠 블록 추가 (TEXT|IMAGE|PLACE)")
    @PostMapping("/{travelogueId}/days/{dayId}/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addBlock(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId,
        @PathVariable Long dayId,
        @Valid @RequestBody TravelogueRequest.AddBlock request
    ) {
        travelogueService.addBlock(travelogueId, dayId, user.userId(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "블록 삭제")
    @DeleteMapping("/{travelogueId}/days/{dayId}/blocks/{blockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlock(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId,
        @PathVariable Long dayId,
        @PathVariable Long blockId
    ) {
        travelogueService.deleteBlock(travelogueId, dayId, blockId, user.userId());
    }

    @Operation(summary = "블록 순서 일괄 변경")
    @PutMapping("/{travelogueId}/days/{dayId}/blocks/reorder")
    public ApiResponse<Void> reorderBlocks(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId,
        @PathVariable Long dayId,
        @Valid @RequestBody TravelogueRequest.ReorderBlocks request
    ) {
        travelogueService.reorderBlocks(travelogueId, dayId, user.userId(), request);
        return ApiResponse.ok();
    }

    // ── 좋아요 / 스크랩 ──────────────────────────

    @Operation(summary = "좋아요")
    @PostMapping("/{travelogueId}/likes")
    public ApiResponse<Void> like(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId
    ) {
        travelogueService.likeTravelogue(travelogueId, user.userId());
        return ApiResponse.ok();
    }

    @Operation(summary = "좋아요 취소")
    @DeleteMapping("/{travelogueId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId
    ) {
        travelogueService.unlikeTravelogue(travelogueId, user.userId());
    }

    @Operation(summary = "동선 스크랩 — Travelogue의 PLACE 블록을 내 Itinerary로 복사")
    @PostMapping("/{travelogueId}/scraps")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItineraryResponse.Created> scrap(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long travelogueId
    ) {
        return ApiResponse.ok(travelogueService.scrapTravelogue(travelogueId, user.userId()));
    }
}
