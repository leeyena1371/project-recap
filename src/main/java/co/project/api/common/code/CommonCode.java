package co.project.api.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 실제 개발에 사용하는 용도
 */
public class CommonCode {

    // 성공 코드
    public static final int SUCCESS_CODE = 0;

    // 서버 오류 코드
    public static final int COMMON_ERROR_CODE = 10000;

    // 기본 파라미터 오류 코드
    public static final int COMMON_PARAM_ERROR_CODE = 10004;

    /**
     * Country enum
     */
    @RequiredArgsConstructor
    @Getter
    public enum Country{

        KR("kr");

        private final String value;
    }

    /**
     * Language enum
     */
    @RequiredArgsConstructor
    @Getter
    public enum Language{

        KO("ko");

        private final String value;
    }
}
