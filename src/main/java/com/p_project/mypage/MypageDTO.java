package com.p_project.mypage;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MypageDTO {

    private Integer weekDiaryNum;
    private Integer weekBookReportNum;
    private Integer weekTotalNum;
    private String email;
    private String nickName;
    private String profileURL;



}
