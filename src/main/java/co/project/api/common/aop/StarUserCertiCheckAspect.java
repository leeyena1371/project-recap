package co.project.api.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Aspect
@Component
public class StarUserCertiCheckAspect {


    @Value("${app-group.default}")
    private String defaultAppGroup;

    @Around("@within(co.dearu.live.api.common.annotation.StarUserCertiCheck)")
    public Object isStarUser(ProceedingJoinPoint pjp) throws Throwable {
        log.info("AOP >> BroadCaster API calling user check");

        // 1. interceptor에서 생성한 certikey 기반 userIdx 조회
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String userIdx = (String) request.getAttribute("userIdx");

        // 2. api 파라미터에서 appGroup 조회 - filter에서 사용된 것과 같은 로직
        String keyNameAppGroup = "appGroup";
        String appGroup = "";
        {
            if (RequestMethod.GET.name().equals(request.getMethod())) {

                if (null != request.getParameter(keyNameAppGroup) || "null".equals(request.getParameter(keyNameAppGroup))) {
                    appGroup = request.getParameter(keyNameAppGroup);
                } else {
                    appGroup = defaultAppGroup;
                }
            }
            else {
                InputStream inputStream = request.getInputStream();
                byte[] rawData = IOUtils.toByteArray(inputStream);
                String bodyParameter = new String(rawData, StandardCharsets.UTF_8);
                JSONObject bodyParam = new JSONObject(bodyParameter);

                if(bodyParam.has(keyNameAppGroup)){
                    appGroup = bodyParam.getString(keyNameAppGroup);
                }
            }
        }

        // 3. 버블 아티스트인지 확인 및 로직 실행 여부 판단
        boolean isBubbleArtist = redisClusterKeyUtil.isBubbleArtist(appGroup, userIdx);

        if(isBubbleArtist) {
            // 등록된 로직 실행
            return pjp.proceed();
        }

        // 3-1. closeLive API 호출일 경우 매니저 계정인지 확인
        String requestURI = request.getRequestURI();

        boolean isManager = false;

        if(Objects.equals(pjp.getSignature().getName(), "closeLive")) {
            String liveID = requestURI.split("/")[3];
            String staruseridx = LiveHelper.getLiveInfoVO(redisClusterKeyUtil, liveID).getStaruseridx();
            isManager = redisClusterKeyUtil.checkLiveManager(staruseridx, userIdx);
        }

        if(isManager) {
            // 등록된 로직 실행
            return pjp.proceed();
        }

        throw new CommonException.DefaultParameterException("It's not staruser");

    }

}
