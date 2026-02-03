package com.p_project.writing;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TitleResponseDTO {
    /** AI가 보내준 제목 후보 3개 (제안 요청 시에만 채워짐) */
    private List<String> titles;
    /** 저장한 제목 (사용자가 고른 제목을 저장했을 때 반환) */
    private String title;
}
