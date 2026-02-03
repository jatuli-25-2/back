package com.p_project.calendar;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayEmotionDTO {
    private LocalDate date;
    private String emotion;  // 분노, 혐오, 두려움, 기쁨, 중립, 슬픔, 놀람
}
