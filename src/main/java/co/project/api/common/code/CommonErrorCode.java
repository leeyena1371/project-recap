package co.project.api.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CommonErrorCode {


    COMMON_ERR(10000)
    , COMMON_ERR_PARAM(10004)
    , COMMON_ERR_LOGIN(10005)
    , PARAMETER_ERR(10004);

    private final int value;
}
