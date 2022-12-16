package co.project.api.recap.dto;

import lombok.*;
import org.hibernate.annotations.Parameter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DailyReportListReqDTO extends CommonReqDTO {
    @Builder.Default
    private String starUserIdx = "";

}