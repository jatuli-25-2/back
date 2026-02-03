package com.p_project.AI;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TitleRequest {
    private String mode;
    private String finalText;
    private String dominantEmotion;
    private List<String> titles;
    private Integer selectedIndex;
    private String customTitle;
}
