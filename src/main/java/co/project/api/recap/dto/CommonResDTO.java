package co.project.api.recap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResDTO<T>{

    // response code
    @JsonInclude(JsonInclude.Include.ALWAYS)
    /* return 코드, 성공일때 0, 실패일때 다른 코드로 내려감 */
    private int code;

    // 클라이언트에 필요한 응답 데이터
    // 실제 리턴될 데이터 Single Object
    // 실제 리턴될 데이터 Array Object
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /* 클라이언트에서 필요한 응답 데이터 */
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    /* 클라이언트에서 보여줄 메세지 */
    private String msg;

    // API 호출 고유값
    @JsonInclude(JsonInclude.Include.ALWAYS)
    /* 해당 API호출의 고유값 */
    private String traceId;

}
