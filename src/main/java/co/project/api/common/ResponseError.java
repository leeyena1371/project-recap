package co.project.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseError{

    // 정의된 오류 코드 (ErrorCode 참조
    private int code;

    // 클라이언트에서 확인할 오류 관련 메시지
    private String detail;

    // (option) 클라이언트에서 toast, popup 등으로 보여줄 메시지
    private String message;
}
