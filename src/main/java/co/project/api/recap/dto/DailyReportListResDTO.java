package co.project.api.recap.dto;

import lombok.*;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportListResDTO {

    @Builder.Default
    private int authority = 0;//유료 아이템 권한(  0 : 미 사용, 1 : 사용 가능, 2 : 사용 중)

    @Builder.Default
    private String artistIP = "";

    @Builder.Default
    private String groupColor = "";

}