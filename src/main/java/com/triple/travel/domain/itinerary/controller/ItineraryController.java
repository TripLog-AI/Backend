package com.triple.travel.domain.itinerary.controller;

import com.triple.travel.common.dto.ApiResponse;
import com.triple.travel.domain.itinerary.dto.ItineraryRequest;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.itinerary.dto.PlaceSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "Itinerary", description = "여행 일정 관리 API")
@RestController
@RequestMapping("/api/v1/itineraries")
public class ItineraryController {

    // ──────────────────────────────────────────
    // Mock 더미 데이터 팩토리
    // ──────────────────────────────────────────

    private PlaceSummary mockPlace(Long id, String name, String address, String category,
                                   double lat, double lng, double rating) {
        return new PlaceSummary(id, name, address, category,
            BigDecimal.valueOf(lat), BigDecimal.valueOf(lng),
            BigDecimal.valueOf(rating), "https://images.unsplash.com/photo-mock-" + id);
    }

    private ItineraryResponse.Detail mockItineraryDetail(Long id) {
        PlaceSummary eiffel     = mockPlace(1L, "에펠탑",            "Champ de Mars, Paris",        "ATTRACTION", 48.8584, 2.2945, 4.7);
        PlaceSummary laduree    = mockPlace(2L, "라뒤레 샹젤리제점", "75 Av. des Champs-Élysées",   "CAFE",       48.8698, 2.3078, 4.5);
        PlaceSummary boucheron  = mockPlace(3L, "르 부쉐롱",         "1 Pl. Vendôme, Paris",        "RESTAURANT", 48.8672, 2.3308, 4.8);

        PlaceSummary altLe      = mockPlace(4L, "르 크리용",         "10 Pl. de la Concorde",       "RESTAURANT", 48.8655, 2.3212, 4.6);
        PlaceSummary altCafe    = mockPlace(5L, "카페 드 플로르",    "172 Bd Saint-Germain",        "CAFE",       48.8540, 2.3329, 4.4);

        PlaceSummary louvre     = mockPlace(6L, "루브르 박물관",     "Rue de Rivoli, Paris",        "ATTRACTION", 48.8606, 2.3376, 4.8);
        PlaceSummary altLouvre1 = mockPlace(7L, "오르세 미술관",     "1 Rue de la Légion d'Honneur","ATTRACTION", 48.8600, 2.3266, 4.7);
        PlaceSummary altLouvre2 = mockPlace(8L, "퐁피두 센터",       "Place Georges-Pompidou",      "ATTRACTION", 48.8607, 2.3522, 4.3);

        ItineraryResponse.SlotDetail slot1 = new ItineraryResponse.SlotDetail(
            101L, 1, LocalTime.of(10, 0), "MORNING", 120, "맑은 날 꼭 가야 할 곳",
            eiffel,
            List.of(
                new ItineraryResponse.AlternativeDetail(201L, 1, altLouvre1),
                new ItineraryResponse.AlternativeDetail(202L, 2, altLouvre2)
            )
        );

        ItineraryResponse.SlotDetail slot2 = new ItineraryResponse.SlotDetail(
            102L, 2, LocalTime.of(12, 30), "LUNCH", 90, null,
            laduree,
            List.of(
                new ItineraryResponse.AlternativeDetail(203L, 1, altLe),
                new ItineraryResponse.AlternativeDetail(204L, 2, altCafe)
            )
        );

        ItineraryResponse.SlotDetail slot3 = new ItineraryResponse.SlotDetail(
            103L, 3, LocalTime.of(15, 0), "AFTERNOON", 180, null,
            louvre,
            List.of()
        );

        ItineraryResponse.SlotDetail slot4 = new ItineraryResponse.SlotDetail(
            104L, 4, LocalTime.of(19, 30), "DINNER", 120, "분위기 최고",
            boucheron,
            List.of(
                new ItineraryResponse.AlternativeDetail(205L, 1, altLe),
                new ItineraryResponse.AlternativeDetail(206L, 2, altCafe)
            )
        );

        ItineraryResponse.DayDetail day1 = new ItineraryResponse.DayDetail(
            10L, 1, LocalDate.of(2024, 8, 1), "파리 도착 후 첫날",
            List.of(slot1, slot2, slot3, slot4)
        );

        PlaceSummary versailles = mockPlace(9L,  "베르사유 궁전",  "Place d'Armes, Versailles", "ATTRACTION", 48.8049, 2.1204, 4.7);
        PlaceSummary montmartre = mockPlace(10L, "몽마르트르 언덕", "Rue Foyatier, Paris",      "ATTRACTION", 48.8867, 2.3431, 4.6);

        ItineraryResponse.SlotDetail slot5 = new ItineraryResponse.SlotDetail(
            105L, 1, LocalTime.of(9, 0), "MORNING", 240, null,
            versailles, List.of()
        );
        ItineraryResponse.SlotDetail slot6 = new ItineraryResponse.SlotDetail(
            106L, 2, LocalTime.of(17, 0), "AFTERNOON", 90, null,
            montmartre, List.of()
        );

        ItineraryResponse.DayDetail day2 = new ItineraryResponse.DayDetail(
            11L, 2, LocalDate.of(2024, 8, 2), null,
            List.of(slot5, slot6)
        );

        return new ItineraryResponse.Detail(
            id, "파리 3박 4일 완벽 여행", "Paris", "France",
            LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 4),
            "COUPLE", List.of("CULTURE", "FOOD"), "NORMAL",
            "AI_GENERATED", "ACTIVE",
            List.of(day1, day2),
            LocalDateTime.of(2024, 7, 1, 10, 0),
            LocalDateTime.of(2024, 7, 2, 15, 30)
        );
    }

    // ──────────────────────────────────────────
    // 엔드포인트
    // ──────────────────────────────────────────

    @Operation(summary = "내 여행 목록 조회")
    @GetMapping
    public ApiResponse<List<ItineraryResponse.Summary>> getMyItineraries(
        @Parameter(description = "상태 필터 (DRAFT|ACTIVE|ARCHIVED)") @RequestParam(required = false) String status,
        @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10") int size
    ) {
        List<ItineraryResponse.Summary> mockList = List.of(
            new ItineraryResponse.Summary(1L, "파리 3박 4일 완벽 여행", "Paris", "France",
                LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 4),
                "AI_GENERATED", "ACTIVE", LocalDateTime.of(2024, 7, 1, 10, 0)),
            new ItineraryResponse.Summary(2L, "도쿄 혼자 5일", "Tokyo", "Japan",
                LocalDate.of(2024, 9, 10), LocalDate.of(2024, 9, 14),
                "YOUTUBE_PARSED", "DRAFT", LocalDateTime.of(2024, 7, 5, 14, 0)),
            new ItineraryResponse.Summary(3L, "바르셀로나 가우디 투어", "Barcelona", "Spain",
                LocalDate.of(2024, 10, 3), LocalDate.of(2024, 10, 6),
                "MANUAL", "DRAFT", LocalDateTime.of(2024, 7, 8, 9, 30))
        );
        return ApiResponse.success(mockList);
    }

    @Operation(summary = "여행 일정 상세 조회 (days + slots + alternatives 포함)")
    @GetMapping("/{itineraryId}")
    public ApiResponse<ItineraryResponse.Detail> getItinerary(
        @PathVariable Long itineraryId
    ) {
        return ApiResponse.success(mockItineraryDetail(itineraryId));
    }

    @Operation(summary = "빈 일정 직접 생성")
    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItineraryResponse.Created> createManual(
        @Valid @RequestBody ItineraryRequest.CreateManual request
    ) {
        return ApiResponse.success(new ItineraryResponse.Created(42L));
    }

    @Operation(summary = "일정 메타 정보 수정 (제목, 도시, 날짜)")
    @PatchMapping("/{itineraryId}")
    public ApiResponse<ItineraryResponse.Created> updateMeta(
        @PathVariable Long itineraryId,
        @RequestBody ItineraryRequest.UpdateMeta request
    ) {
        return ApiResponse.success(new ItineraryResponse.Created(itineraryId));
    }

    @Operation(summary = "여행 일정 삭제")
    @DeleteMapping("/{itineraryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItinerary(@PathVariable Long itineraryId) {
        // mock: 204 No Content
    }

    // ── AI 생성 ──────────────────────────────────

    @Operation(summary = "AI 일정 생성 요청 (비동기 - Job 등록 후 requestId 반환)")
    @PostMapping("/ai/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<ItineraryResponse.JobStatus> requestAiGenerate(
        @Valid @RequestBody ItineraryRequest.GenerateByAi request
    ) {
        return ApiResponse.success(
            new ItineraryResponse.JobStatus(9001L, "PENDING", null, null)
        );
    }

    @Operation(summary = "AI 생성 상태 폴링")
    @GetMapping("/ai/requests/{requestId}")
    public ApiResponse<ItineraryResponse.JobStatus> pollAiRequest(
        @PathVariable Long requestId
    ) {
        // Mock: 항상 COMPLETED 응답으로 프론트 개발 편의 제공
        return ApiResponse.success(
            new ItineraryResponse.JobStatus(requestId, "COMPLETED", 1L, null)
        );
    }

    // ── YouTube 파싱 ─────────────────────────────

    @Operation(summary = "YouTube URL 파싱 요청 (비동기)")
    @PostMapping("/youtube/parse")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<ItineraryResponse.JobStatus> requestYoutubeParse(
        @Valid @RequestBody ItineraryRequest.ParseYoutube request
    ) {
        return ApiResponse.success(
            new ItineraryResponse.JobStatus(9002L, "PENDING", null, null)
        );
    }

    @Operation(summary = "YouTube 파싱 상태 폴링")
    @GetMapping("/youtube/requests/{requestId}")
    public ApiResponse<ItineraryResponse.JobStatus> pollYoutubeRequest(
        @PathVariable Long requestId
    ) {
        return ApiResponse.success(
            new ItineraryResponse.JobStatus(requestId, "COMPLETED", 2L, null)
        );
    }

    // ── Day 관리 ─────────────────────────────────

    @Operation(summary = "Day 추가")
    @PostMapping("/{itineraryId}/days")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addDay(
        @PathVariable Long itineraryId,
        @Valid @RequestBody ItineraryRequest.AddDay request
    ) {
        return ApiResponse.success();
    }

    @Operation(summary = "Day 날짜/메모 수정")
    @PatchMapping("/{itineraryId}/days/{dayId}")
    public ApiResponse<Void> updateDay(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @RequestBody ItineraryRequest.UpdateDay request
    ) {
        return ApiResponse.success();
    }

    @Operation(summary = "Day 삭제 (하위 슬롯 cascade 삭제)")
    @DeleteMapping("/{itineraryId}/days/{dayId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDay(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId
    ) {
        // mock: 204 No Content
    }

    // ── Slot 관리 ────────────────────────────────

    @Operation(summary = "슬롯 추가")
    @PostMapping("/{itineraryId}/days/{dayId}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addSlot(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @Valid @RequestBody ItineraryRequest.AddSlot request
    ) {
        return ApiResponse.success();
    }

    @Operation(summary = "슬롯 수정 (시각, 순서, 메모, 체류시간)")
    @PatchMapping("/{itineraryId}/days/{dayId}/slots/{slotId}")
    public ApiResponse<Void> updateSlot(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId,
        @RequestBody ItineraryRequest.UpdateSlot request
    ) {
        return ApiResponse.success();
    }

    @Operation(summary = "슬롯 삭제")
    @DeleteMapping("/{itineraryId}/days/{dayId}/slots/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId
    ) {}

    @Operation(summary = "대안 장소 목록 조회 (AI 생성 시 제공된 2개)")
    @GetMapping("/{itineraryId}/days/{dayId}/slots/{slotId}/alternatives")
    public ApiResponse<List<ItineraryResponse.AlternativeDetail>> getAlternatives(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId
    ) {
        List<ItineraryResponse.AlternativeDetail> mock = List.of(
            new ItineraryResponse.AlternativeDetail(201L, 1,
                mockPlace(4L, "오르세 미술관", "1 Rue de la Légion d'Honneur", "ATTRACTION", 48.86, 2.3266, 4.7)),
            new ItineraryResponse.AlternativeDetail(202L, 2,
                mockPlace(5L, "퐁피두 센터", "Place Georges-Pompidou", "ATTRACTION", 48.8607, 2.3522, 4.3))
        );
        return ApiResponse.success(mock);
    }

    @Operation(summary = "장소 교체 (Swap) - 대안 장소와 현재 장소를 트랜잭션 내 교환")
    @PatchMapping("/{itineraryId}/days/{dayId}/slots/{slotId}/swap")
    public ApiResponse<ItineraryResponse.SlotDetail> swapPlace(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @PathVariable Long slotId,
        @Valid @RequestBody ItineraryRequest.SwapPlace request
    ) {
        // Mock: 교체 완료된 슬롯 반환
        PlaceSummary swapped = mockPlace(4L, "오르세 미술관",
            "1 Rue de la Légion d'Honneur", "ATTRACTION", 48.86, 2.3266, 4.7);
        PlaceSummary prevMain = mockPlace(1L, "에펠탑",
            "Champ de Mars, Paris", "ATTRACTION", 48.8584, 2.2945, 4.7);

        ItineraryResponse.SlotDetail updated = new ItineraryResponse.SlotDetail(
            slotId, 1, LocalTime.of(10, 0), "MORNING", 120, null,
            swapped,
            List.of(new ItineraryResponse.AlternativeDetail(request.alternativeId(), 1, prevMain))
        );
        return ApiResponse.success(updated);
    }

    @Operation(summary = "슬롯 순서 일괄 변경")
    @PutMapping("/{itineraryId}/days/{dayId}/slots/reorder")
    public ApiResponse<Void> reorderSlots(
        @PathVariable Long itineraryId,
        @PathVariable Long dayId,
        @Valid @RequestBody ItineraryRequest.ReorderSlots request
    ) {
        return ApiResponse.success();
    }
}
