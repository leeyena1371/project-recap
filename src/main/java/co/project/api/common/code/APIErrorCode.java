package co.project.api.common.code;

import lombok.Builder;
import lombok.Getter;

/**
 * API 별로 내려가는 오류코드
 */
@Builder
@Getter
public class APIErrorCode {

    /* 라이브 조회 불가 에러 코드 */
    public static final int LIVE_INFO_NOT_EXIST = 20000;

    /* 15세 미만 아티스트 방송 불가 에러 코드 */
    public static final int BC_TEENAGER_ERROR_CODE = 20002;

    /* 라이브 조회용 hash 기간 만료 코드 */
    public static final int HASH_INFO_EXPIRED_CODE = 20005;

    /* 다시보기 url 조회용 hash 기간 만료 코드 */
    public static final int HASH_REPLAY_EXPIRED_CODE = 20006;

    /* 재생 가능 시간 만료 코드 */
    public static final int REPLAY_IMPOSSIBLE_TIME_CODE = 20007;

    /* 트랜스코딩 미완료 */
    public static final int TRANSCODING_INCOMPLETE_CODE = 20008;

    /* 다시보기 미제공 */
    public static final int REPLAY_NOT_PROVIDED_CODE = 20009;


}
