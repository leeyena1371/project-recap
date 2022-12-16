package co.project.api.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class Request{

    // api 요청 공통 데이터
    @NotBlank(message = "디바이스키는 필수값잆니다.")
    private String deviceKey;

    @NotNull(message = "언어는 필수값입니다.")
    private String language;

    @NotNull(message = "지역은 필수값입니다.")
    private String locale;

    @NotNull(message = "버전은 필수값입니다.")
    private String version;

    @NotNull(message = "os 타입은 필수값입니다.")
    private int osType;

    @NotNull(message = "앱그룹은 필수값입니다.")
    private String appGroup;

}
