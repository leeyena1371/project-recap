package co.project.api.common;

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
public class Response<T>{

    // 클라이언트에 필요한 응답 데이터
    // 실제 리턴될 데이터 Single Object
    // 실제 리턴될 데이터 Array Object
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // API 호출 고유값
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String traceId;

    // 에러 관련 객체
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResponseError error;

}
