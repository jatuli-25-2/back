package com.p_project.writing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TitleRequestDTO {
    private Long sessionId;
    /** 사용자가 고른 제목 (AI 추천 3개 중 하나이거나 직접 입력). 있으면 이걸 글 제목으로 저장 */
    private String title;
}
