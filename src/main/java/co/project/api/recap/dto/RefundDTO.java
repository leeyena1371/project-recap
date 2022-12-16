package co.project.api.recap.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Getter
@Setter
@ToString
public class RefundDTO {

///**
// * 직접 검증을 하고 싶을 때 사용
// */
//    @AssertTrue
//    @AssertFalse
//
///**
// * 문자열을 다룰 때 사용
// */
//    @NotNull // null 불가능
//    @NotEmpty // null, 빈 문자열(스페이스 포함X) 불가
//    @NotBlank // null, 빈 문자열, 스페이스만 포함한 문자열 불가
//    @Size(min=?, max=?) // 최소 길이, 최대 길이 제한
//    @Null // null만 가능
//
///**
// * 숫자를 다룰 때 사용
// */
//    @Positive // 양수만 허용
//    @PositiveOrZero // 양수와 0만 허용
//    @Negative // 음수만 허용
//    @NegativeOrZero // 음수와 0만 허용
//    @Min(?) // 최소값 제한
//    @Max(?) // 최대값 제한
//
//    /**
//     * 정규식 관련
//     */
//    @Email // 이메일 형식만가능 (기본 제공)
//    @Pattern(regexp="?") // 직접 정규식을 쓸 수 있음


    /**
     * 어플 그룹 (필수)
     */
    @NotNull
    @NotEmpty
    private String appGroup;

    /**
     * 사용자 고유 번호 (필수)
     */
    @NotNull
    @NotEmpty
    private String externalID;

    /**
     * 중국앱 구매 번호 (필수)
     */
    @NotNull
    @NotEmpty
    private String purchaseID;

    /**
     * 중국앱 환불 요청 시간 (UTC+0 기준 Timestamp in milliseconds) (필수)
     */
    @Positive
    private long canceledDT;

    /**
     * 상품 구분 (3:코인상품, 4:starTalk상품) (필수)
     */
    @Positive
    private int productType;

    /**
     * 상품 결제 수단 (8:중국향 weChat pay 결제) (필수)
     */
    @Positive
    private int paymentMethod;

    /**
     * 위변조 체크 값 (필수)
     */
    @NotNull
    @NotEmpty
    private String checksum;

}
