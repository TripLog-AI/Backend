package com.triple.travel.domain.youtube.service;

import com.triple.travel.common.exception.EntityNotFoundException;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.itinerary.entity.Itinerary;
import com.triple.travel.domain.itinerary.entity.ItineraryDay;
import com.triple.travel.domain.itinerary.entity.ItinerarySlot;
import com.triple.travel.domain.itinerary.repository.ItineraryDayRepository;
import com.triple.travel.domain.itinerary.repository.ItineraryRepository;
import com.triple.travel.domain.itinerary.repository.ItinerarySlotRepository;
import com.triple.travel.domain.itinerary.service.ItineraryService;
import com.triple.travel.domain.user.entity.User;
import com.triple.travel.domain.user.repository.UserRepository;
import com.triple.travel.domain.youtube.dto.YoutubeCourseDto;
import com.triple.travel.domain.youtube.entity.YoutubeSource;
import com.triple.travel.domain.youtube.repository.YoutubeSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YoutubeCourseService {

    private final YoutubeSourceRepository youtubeSourceRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayRepository itineraryDayRepository;
    private final ItinerarySlotRepository itinerarySlotRepository;
    private final UserRepository userRepository;

    public List<YoutubeCourseDto.Item> getFeaturedCourses(String city, Long cursor, int size) {
        return youtubeSourceRepository
            .findFeaturedWithCursor(city, cursor, PageRequest.of(0, size))
            .stream()
            .map(this::toItem)
            .toList();
    }

    public YoutubeCourseDto.Detail getCourseDetail(Long courseId) {
        var src = youtubeSourceRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("YOUTUBE_COURSE_NOT_FOUND",
                "YouTube 코스를 찾을 수 없습니다."));

        var preview = itineraryRepository.findSeedByYoutubeSourceId(courseId)
            .map(this::toPreview)
            .orElse(null);

        return new YoutubeCourseDto.Detail(
            src.getId(), src.getVideoId(), src.getTitle(),
            src.getChannelName(), src.getThumbnailUrl(),
            src.getUrl(), preview
        );
    }

    /**
     * YouTube seed 코스를 내 일정으로 저장.
     * 시드 일정(YOUTUBE_SEED)이 등록되어 있으면 days/slots/place 참조까지 deep copy.
     * 없으면 메타만 가진 빈 Itinerary를 생성한다.
     */
    @Transactional
    public ItineraryResponse.Created saveCourse(Long userId, Long courseId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        var src = youtubeSourceRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("YOUTUBE_COURSE_NOT_FOUND",
                "YouTube 코스를 찾을 수 없습니다."));

        Long newItineraryId = itineraryRepository.findSeedByYoutubeSourceId(courseId)
            .map(seed -> deepCopySeedItinerary(user, src, seed))
            .orElseGet(() -> createEmptyFromSource(user, src).getId());

        return new ItineraryResponse.Created(newItineraryId);
    }

    private Long deepCopySeedItinerary(User user, YoutubeSource src, Itinerary seed) {
        // 사용자 소유 일정은 YOUTUBE_PARSED로 저장. (YOUTUBE_SEED는 시스템 시드 전용)
        var copy = itineraryRepository.save(Itinerary.builder()
            .user(user)
            .title(src.getTitle() != null ? src.getTitle() : seed.getTitle())
            .city(seed.getCity())
            .country(seed.getCountry())
            .startDate(seed.getStartDate())
            .endDate(seed.getEndDate())
            .youtubeSource(src)
            .sourceType(Itinerary.SourceType.YOUTUBE_PARSED)
            .build());

        for (ItineraryDay seedDay : seed.getDays()) {
            var newDay = itineraryDayRepository.save(ItineraryDay.builder()
                .itinerary(copy)
                .dayNumber(seedDay.getDayNumber())
                .date(seedDay.getDate())
                .build());

            int slotOrder = 1;
            for (ItinerarySlot seedSlot : seedDay.getSlots()) {
                itinerarySlotRepository.save(ItinerarySlot.builder()
                    .itineraryDay(newDay)
                    .orderIndex(slotOrder++)
                    .slotTime(seedSlot.getSlotTime())
                    .timeCategory(seedSlot.getTimeCategory())
                    .stayDurationMinutes(seedSlot.getStayDurationMinutes())
                    .place(seedSlot.getPlace()) // Place 참조 공유 (캐시 데이터)
                    .build());
            }
        }
        return copy.getId();
    }

    private Itinerary createEmptyFromSource(User user, YoutubeSource src) {
        return itineraryRepository.save(Itinerary.builder()
            .user(user)
            .title(src.getTitle() != null ? src.getTitle() : "유튜브 저장 코스")
            .youtubeSource(src)
            .sourceType(Itinerary.SourceType.YOUTUBE_PARSED)
            .build());
    }

    /**
     * 시드 Itinerary 미리보기. ItineraryService의 toPlaceSummary 재사용.
     * 미리보기는 alternatives 없이 days/slots/place만.
     */
    private ItineraryResponse.Detail toPreview(Itinerary seed) {
        var days = seed.getDays().stream()
            .map(d -> new ItineraryResponse.DayDetail(
                d.getId(), d.getDayNumber(), d.getDate(), d.getMemo(),
                d.getSlots().stream()
                    .map(s -> new ItineraryResponse.SlotDetail(
                        s.getId(), s.getOrderIndex(), s.getSlotTime(),
                        s.getTimeCategory() != null ? s.getTimeCategory().name() : null,
                        s.getStayDurationMinutes(), s.getMemo(),
                        ItineraryService.toPlaceSummary(s.getPlace()),
                        List.of()
                    ))
                    .toList()
            ))
            .toList();

        return new ItineraryResponse.Detail(
            seed.getId(), seed.getTitle(), seed.getCity(), seed.getCountry(),
            seed.getStartDate(), seed.getEndDate(),
            null, List.of(), null,
            seed.getSourceType().name(), seed.getStatus().name(),
            days,
            seed.getCreatedAt(), seed.getUpdatedAt()
        );
    }

    private YoutubeCourseDto.Item toItem(YoutubeSource s) {
        return new YoutubeCourseDto.Item(
            s.getId(), s.getVideoId(), s.getTitle(),
            s.getChannelName(), s.getThumbnailUrl(), s.getUrl()
        );
    }
}
