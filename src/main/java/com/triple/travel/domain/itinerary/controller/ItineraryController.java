package com.triple.travel.domain.itinerary.controller;

import com.triple.travel.common.dto.ApiResponse;
import com.triple.travel.common.security.AuthPrincipal;
import com.triple.travel.domain.itinerary.dto.ItineraryRequest;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.itinerary.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Itinerary", description = "여행 일정 관리 API")
@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    // ── 조회 ─────────────────────────────────────

    @Operation(summary = "내 여행 목록 조회 (커서 페이지네이션)")
    @GetMapping
    public ApiResponse<List<ItineraryResponse.Summary>> getMyItineraries(
        @AuthenticationPrincipal AuthPrincipal user,
        @Parameter(description = "상태 필터 (DRAFT|ACTIVE|ARCHIVED)") @RequestParam(required = false) String status,
        @Parameter(description = "커서 ID (이전 페이지 마지막 id)") @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(itineraryService.getMyItineraries(user.userId(), status, cursor, size));
    }

    @Operation(summary = "여행 일정 상세 조회 (days + slots + alternatives 포함)")
    @GetMapping("/{itineraryId}")
    public ApiResponse<ItineraryResponse.Detail> getItinerary(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId
    ) {
        return ApiResponse.ok(itineraryService.getItinerary(itineraryId, user.userId()));
    }

    // ── 일정 CRUD ────────────────────────────────

    @Operation(summary = "빈 일정 직접 생성")
    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItineraryResponse.Created> createManual(
        @AuthenticationPrincipal AuthPrincipal user,
        @Valid @RequestBody ItineraryRequest.CreateManual request
    ) {
        return ApiResponse.ok(itineraryService.createManual(user.userId(), request));
    }

    @Operation(summary = "일정 메타 정보 수정 (제목, 도시, 날짜)")
    @PatchMapping("/{itineraryId}")
    public ApiResponse<ItineraryResponse.Created> updateMeta(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @RequestBody ItineraryRequest.UpdateMeta request
    ) {
        return ApiResponse.ok(itineraryService.updateMeta(itineraryId, user.userId(), request));
    }

    @Operation(summary = "여행 일정 삭제")
    @DeleteMapping("/{itineraryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItinerary(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId
    ) {
        itineraryService.deleteItinerary(itineraryId, user.userId());
    }

    // ── AI / YouTube 비동기 요청 ──────────────────

    @Operation(summary = "AI 일정 생성 요청 (비동기 — requestId 반환, 폴링 필요)")
    @PostMapping("/ai/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<ItineraryResponse.JobStatus> requestAiGenerate(
        @AuthenticationPrincipal AuthPrincipal user,
        @Valid @RequestBody ItineraryRequest.GenerateByAi request
    ) {
        return ApiResponse.ok(itineraryService.requestAiGenerate(user.userId(), request));
    }

    @Operation(summary = "YouTube URL 파싱 요청 (비동기)")
    @PostMapping("/youtube/parse")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<ItineraryResponse.JobStatus> requestYoutubeParse(
        @AuthenticationPrincipal AuthPrincipal user,
        @Valid @RequestBody ItineraryRequest.ParseYoutube request
    ) {
        return ApiResponse.ok(itineraryService.requestYoutubeParse(user.userId(), request));
    }

    @Operation(summary = "AI/YouTube 생성 상태 폴링 (PENDING|PROCESSING|COMPLETED|FAILED)")
    @GetMapping("/ai/requests/{requestId}")
    public ApiResponse<ItineraryResponse.JobStatus> pollAiRequest(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long requestId
    ) {
        return ApiResponse.ok(itineraryService.pollAiRequest(requestId, user.userId()));
    }

    // ── Day 관리 ─────────────────────────────────

    @Operation(summary = "Day 추가")
    @PostMapping("/{itineraryId}/days")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addDay(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @Valid @RequestBody ItineraryRequest.AddDay request
    ) {
        itineraryService.addDay(itineraryId, user.userId(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "Day 날짜/메모 수정")
    @PatchMapping("/{itineraryId}/days/{dayId}")
    public ApiResponse<Void> updateDay(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @RequestBody ItineraryRequest.UpdateDay request
    ) {
        itineraryService.updateDay(itineraryId, dayId, user.userId(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "Day 삭제 (하위 슬롯 cascade 삭제)")
    @DeleteMapping("/{itineraryId}/days/{dayId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDay(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId
    ) {
        itineraryService.deleteDay(itineraryId, dayId, user.userId());
    }

    // ── Slot 관리 ────────────────────────────────

    @Operation(summary = "슬롯 추가")
    @PostMapping("/{itineraryId}/days/{dayId}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addSlot(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @Valid @RequestBody ItineraryRequest.AddSlot request
    ) {
        itineraryService.addSlot(itineraryId, dayId, user.userId(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "슬롯 수정 (시각, 순서, 메모, 체류시간)")
    @PatchMapping("/{itineraryId}/days/{dayId}/slots/{slotId}")
    public ApiResponse<Void> updateSlot(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId,
        @RequestBody ItineraryRequest.UpdateSlot request
    ) {
        itineraryService.updateSlot(itineraryId, dayId, slotId, user.userId(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "슬롯 삭제")
    @DeleteMapping("/{itineraryId}/days/{dayId}/slots/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId
    ) {
        itineraryService.deleteSlot(itineraryId, dayId, slotId, user.userId());
    }

    @Operation(summary = "슬롯 순서 일괄 변경")
    @PutMapping("/{itineraryId}/days/{dayId}/slots/reorder")
    public ApiResponse<Void> reorderSlots(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @Valid @RequestBody ItineraryRequest.ReorderSlots request
    ) {
        itineraryService.reorderSlots(itineraryId, dayId, user.userId(), request);
        return ApiResponse.ok();
    }

    @Operation(summary = "대안 장소 목록 조회 (AI 생성 시 제공된 2개)")
    @GetMapping("/{itineraryId}/days/{dayId}/slots/{slotId}/alternatives")
    public ApiResponse<List<ItineraryResponse.AlternativeDetail>> getAlternatives(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId
    ) {
        return ApiResponse.ok(itineraryService.getAlternatives(itineraryId, dayId, slotId, user.userId()));
    }

    @Operation(summary = "장소 교체 (Swap) — 대안 장소와 현재 장소를 트랜잭션 내 원자적 교환")
    @PatchMapping("/{itineraryId}/days/{dayId}/slots/{slotId}/swap")
    public ApiResponse<ItineraryResponse.SlotDetail> swapPlace(
        @AuthenticationPrincipal AuthPrincipal user,
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId,
        @Valid @RequestBody ItineraryRequest.SwapPlace request
    ) {
        return ApiResponse.ok(
            itineraryService.swapPlace(itineraryId, dayId, slotId, request, user.userId())
        );
    }
}
