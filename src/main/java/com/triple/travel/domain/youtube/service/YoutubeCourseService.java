package com.triple.travel.domain.youtube.service;

import com.triple.travel.common.exception.EntityNotFoundException;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.itinerary.entity.Itinerary;
import com.triple.travel.domain.itinerary.repository.ItineraryRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YoutubeCourseService {

    private final YoutubeSourceRepository youtubeSourceRepository;
    private final ItineraryRepository itineraryRepository;
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
        return new YoutubeCourseDto.Detail(
            src.getId(), src.getVideoId(), src.getTitle(),
            src.getChannelName(), src.getThumbnailUrl(),
            src.getUrl()
        );
    }

    /**
     * YouTube seed 코스를 내 일정으로 저장. 현재는 메타만 복사한 빈 Itinerary 생성.
     * AI 파싱 결과 시드가 정식으로 들어오면 deep copy 로직으로 확장 예정.
     */
    @Transactional
    public ItineraryResponse.Created saveCourse(Long userId, Long courseId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        var src = youtubeSourceRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("YOUTUBE_COURSE_NOT_FOUND",
                "YouTube 코스를 찾을 수 없습니다."));

        var itinerary = Itinerary.builder()
            .user(user)
            .title(src.getTitle() != null ? src.getTitle() : "유튜브 저장 코스")
            .youtubeSource(src)
            .sourceType(Itinerary.SourceType.YOUTUBE_SEED)
            .build();

        return new ItineraryResponse.Created(itineraryRepository.save(itinerary).getId());
    }

    private YoutubeCourseDto.Item toItem(YoutubeSource s) {
        return new YoutubeCourseDto.Item(
            s.getId(), s.getVideoId(), s.getTitle(),
            s.getChannelName(), s.getThumbnailUrl(), s.getUrl()
        );
    }
}
