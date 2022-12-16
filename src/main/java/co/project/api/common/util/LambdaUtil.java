package co.project.api.common.util;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.*;
import software.amazon.awssdk.services.lambda.*;
import software.amazon.awssdk.services.lambda.model.*;

@Slf4j
public class LambdaUtil {

    /**
     * 람다 함수 호출(비동기식)
     * @param functionName 호출할 람다 Function 이름
     * @param data 전달할 데이터(json string형태)
     */
    public static void invokeLambdaAsync(LambdaClient lambdaClient, String functionName, String data) {
        new Thread(() -> {
            log.info("invokeLambdaAsync start : " + functionName);
//        String funtionNameEnv = CommonUtil.getEnv()+"_"+functionName;
            // Lambda 함수 호출용 request 객체 구성
            InvokeRequest req = InvokeRequest.builder()
                    .functionName(functionName)
                    .invocationType(InvocationType.EVENT)
                    .payload(SdkBytes.fromUtf8String(data))
                    .build();

            // Lambda 함수 호출 실행
            InvokeResponse res = lambdaClient.invoke(req);
            log.info("invokeLambdaAsync result : " + res.statusCode());
        }).start();
    }

    /**
     * env까지 고려하여 실제 lambda 함수명을 조회
     * @param lambdaFunc 조회할 함수
     * @return
     */
    public static String getFunctionName(String profile, String lambdaFunc) {
        return profile.split("-")[1]+"_livetalk_" + lambdaFunc;
    }
}
