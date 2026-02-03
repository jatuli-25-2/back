package com.p_project.calendar;

import com.p_project.user.UserEntity;
import com.p_project.user.UserRepository;
import com.p_project.writing.WritingSessionEntity;
import com.p_project.writing.WritingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final WritingSessionRepository writingSessionRepository;
    private final UserRepository userRepository;

    /**
     * 내 캘린더: 해당 월의 날짜별 대표 감정 + 월별 일기/독후감 개수
     * - 날짜별: 일기 > 독후감 우선, 동일 타입이면 처음 쓴 것
     */
    public CalendarDTO getCalendarSummary(Long userId, LocalDate date) {
        LocalDate targetMonth = (date != null) ? date : LocalDate.now();
        int year = targetMonth.getYear();
        int month = targetMonth.getMonthValue();

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        List<WritingSessionEntity> writings = writingSessionRepository.findByUserIdAndMonth(userId, start, end);

        List<DayEmotionDTO> dailyEmotions = buildDailyEmotions(writings);
        int diaryCount = (int) writings.stream().filter(w -> w.getType() == WritingSessionEntity.Type.diary).count();
        int bookCount = (int) writings.stream().filter(w -> w.getType() == WritingSessionEntity.Type.book).count();

        return CalendarDTO.builder()
                .year(year)
                .month(month)
                .diaryCount(diaryCount)
                .bookCount(bookCount)
                .dailyEmotions(dailyEmotions)
                .build();
    }

    /**
     * 친구 캘린더: 동일 로직, friendId 기준
     */
    public CalendarDTO getFriendCalendarSummary(Long userId, Long friendId, LocalDate date) {
        LocalDate targetMonth = (date != null) ? date : LocalDate.now();
        int year = targetMonth.getYear();
        int month = targetMonth.getMonthValue();

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        List<WritingSessionEntity> writings = writingSessionRepository.findByUserIdAndMonth(friendId, start, end);

        List<DayEmotionDTO> dailyEmotions = buildDailyEmotions(writings);
        int diaryCount = (int) writings.stream().filter(w -> w.getType() == WritingSessionEntity.Type.diary).count();
        int bookCount = (int) writings.stream().filter(w -> w.getType() == WritingSessionEntity.Type.book).count();

        String friendNickName = userRepository.findById(friendId)
                .map(UserEntity::getNickname)
                .orElse("Unknown");

        return CalendarDTO.builder()
                .userId(userId)
                .friendId(friendId)
                .friendNickName(friendNickName)
                .year(year)
                .month(month)
                .diaryCount(diaryCount)
                .bookCount(bookCount)
                .dailyEmotions(dailyEmotions)
                .build();
    }

    /**
     * 날짜별 대표 감정: 일기 우선, 동일 타입이면 처음 쓴 것
     */
    private List<DayEmotionDTO> buildDailyEmotions(List<WritingSessionEntity> writings) {
        Map<LocalDate, List<WritingSessionEntity>> byDate = writings.stream()
                .collect(Collectors.groupingBy(w -> w.getCreatedAt().toLocalDate()));

        List<DayEmotionDTO> result = new ArrayList<>();
        for (LocalDate d : byDate.keySet()) {
            List<WritingSessionEntity> list = byDate.get(d);
            WritingSessionEntity primary = list.stream()
                    .sorted(Comparator
                            .comparing(WritingSessionEntity::getType)  // diary(0) before book(1)
                            .thenComparing(WritingSessionEntity::getCreatedAt))
                    .findFirst()
                    .orElse(null);
            if (primary != null && primary.getEmotion() != null && !primary.getEmotion().isBlank()) {
                result.add(DayEmotionDTO.builder()
                        .date(d)
                        .emotion(primary.getEmotion())
                        .build());
            }
        }
        result.sort(Comparator.comparing(DayEmotionDTO::getDate));
        return result;
    }

    /**
     * 해당 날짜에 쓴 글 목록 (타입, 날짜, 제목, 감정) - 스크롤 목록용. 일기 우선, 같은 타입이면 먼저 쓴 순.
     */
    public List<DailyWritingItemDTO> getDailyWritings(Long userId, LocalDate date) {
        List<WritingSessionEntity> list = writingSessionRepository.findByUserIdAndDate(userId, date);
        return list.stream()
                .map(w -> DailyWritingItemDTO.builder()
                        .id(w.getId())
                        .type(w.getType().name())
                        .date(w.getCreatedAt().toLocalDate())
                        .title(w.getTitle() != null ? w.getTitle() : "(제목 없음)")
                        .emotion(w.getEmotion() != null ? w.getEmotion() : "")
                        .build())
                .collect(Collectors.toList());
    }
}
