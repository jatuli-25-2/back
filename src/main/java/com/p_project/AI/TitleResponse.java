package com.p_project.AI;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TitleResponse {
    private List<String> titles;
    private String finalTitle;
    private boolean allowCustom;
    private String stage;  // "suggest" or "confirm"
}
