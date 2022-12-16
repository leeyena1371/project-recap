package co.project.api.common.exception;

import co.project.api.common.Response;
import co.project.api.common.ResponseError;
import co.project.api.common.code.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ExceptionAdvice extends Throwable {

    /**
     * 데이터 유효성 체크
     * @param ex
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Response validException(MethodArgumentNotValidException ex) {

        ResponseError error = ResponseError.builder()
                .code(CommonErrorCode.COMMON_ERR_PARAM.getValue())
                .detail(ex.getFieldError().getDefaultMessage())
                .message("필수값을 입력하세요")
                .build();

        return Response.builder()
                .error(error)
                .build();
    }

}
