package com.p_project.calendar;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarDTO {

    /** 조회한 연도 */
    private Integer year;
    /** 조회한 월 */
    private Integer month;
    /** 해당 월 일기 개수 */
    private Integer diaryCount;
    /** 해당 월 독후감 개수 */
    private Integer bookCount;
    /** 날짜별 대표 감정 (일기 우선, 동일 타입이면 처음 쓴 것) */
    private List<DayEmotionDTO> dailyEmotions;

    /** 친구 캘린더용 */
    private Long userId;
    private Long friendId;
    private String friendNickName;
}
