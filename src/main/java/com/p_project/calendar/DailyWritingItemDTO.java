package com.p_project.calendar;

import lombok.*;

import java.time.LocalDate;

/** 캘린더에서 날짜 클릭 시 해당 날짜에 쓴 글 목록 한 건 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyWritingItemDTO {
    private Long id;           // sessionId, 클릭 시 상세 이동용
    private String type;      // "diary" | "book"
    private LocalDate date;
    private String title;
    private String emotion;
}
