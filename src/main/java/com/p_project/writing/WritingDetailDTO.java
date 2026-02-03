package com.p_project.writing;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 글 한 건 상세 (캘린더에서 글 클릭 시 이동한 화면용) */
@Getter
@Builder
public class WritingDetailDTO {
    private Long id;
    private String type;       // "diary" | "book"
    private LocalDateTime createdAt;
    private LocalDate date;
    private String title;
    private String content;
    private String emotion;
    private String recommendTitle;
    private String recommendGenre;
}
