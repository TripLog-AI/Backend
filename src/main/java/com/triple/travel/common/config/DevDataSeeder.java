package com.triple.travel.common.config;

import com.triple.travel.domain.youtube.entity.YoutubeSource;
import com.triple.travel.domain.youtube.repository.YoutubeSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * dev 프로필 부팅 시 데모용 시드 데이터를 채워 넣는다.
 * H2 in-memory + ddl-auto=create-drop 이라 매 부팅마다 새로 생성됨.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final YoutubeSourceRepository youtubeSourceRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (youtubeSourceRepository.count() > 0) return;

        seedYoutubeCourse("dQw4w9WgXcQ", "도쿄 2박 3일 완벽 가이드 | 현지인이 추천하는 숨은 맛집", "여행하는 삶");
        seedYoutubeCourse("xvFZjo5PgG0", "파리 혼자 여행 4박 5일 Vlog | 에펠탑, 루브르, 몽마르트", "솔로트래블러");
        seedYoutubeCourse("C0DPdy98e4c", "바르셀로나 3일 루트 | 가우디 건축 투어 완전정복", "건축여행자");

        log.info("DevDataSeeder: seeded {} YouTube featured courses", youtubeSourceRepository.count());
    }

    private void seedYoutubeCourse(String videoId, String title, String channel) {
        var source = YoutubeSource.builder()
            .videoId(videoId)
            .url("https://www.youtube.com/watch?v=" + videoId)
            .title(title)
            .channelName(channel)
            .thumbnailUrl("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg")
            .featured(true)
            .build();
        source.markCompleted();
        youtubeSourceRepository.save(source);
    }
}
