package co.project.api.common.config.interceptor;

//import co.dearu.live.api.common.config.redis.RedisRepository;

import co.dearu.live.api.common.annotation.NoSignHandler;
import co.dearu.live.api.common.exception.CommonException;
import co.dearu.live.api.common.util.RedisClusterKeyUtil;
import co.dearu.live.api.common.util.VersionHelperUtil;
import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@Component
public class SignKeyCheckInterceptor implements HandlerInterceptor {

    @Autowired
    ServletConfig servletConfig;

    @Value("${app-group.default}")
    private String defaultAppGroup;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String keyNameDeviceKey = "deviceKey";
        String keyNameAppGroup = "appGroup";
        String keyNameAuthorization = "Authorization";

        log.info("path ----------------");
        log.info(request.getRequestURI());

        try{
            NoSignHandler noSignHandler = ((HandlerMethod)handler).getMethodAnnotation(NoSignHandler.class);
            if(noSignHandler != null){
                log.info("noSignHandler - 인증 미사용 API");
                return true;
            }
        }
        catch (Exception e){
            log.info("등록되지않은 api 호출" + e.getMessage());
            return false;
        }

        // 1. Authorization 정보 파싱
        String certiKey = "";
        String[] authList = null;
        String authorization = request.getHeader(keyNameAuthorization);

        if (authorization != null && authorization != "") {
            authList = authorization.split(" ");
            if (authList.length == 2 &&
                    ("token".equals(authList[0].toLowerCase(Locale.ROOT)) || "bearer".equals(authList[0].toLowerCase(Locale.ROOT)))
            ) {
                certiKey = authList[1];
            }
        } else {
            throw new CommonException.DefaultParameterException("you don't have Authorization");
        }

        // 2. 필수 정보 확인 (deviceKey, appGroup)
        String deviceKey = "";
        String appGroup = "";
        {
            // 2-1. HTTP Get - Value에서 조회
            if (RequestMethod.GET.name().equals(request.getMethod())) {
                deviceKey = request.getParameter(keyNameDeviceKey);

                if (null != request.getParameter(keyNameAppGroup) || "null".equals(request.getParameter(keyNameAppGroup))) {
                    appGroup = request.getParameter(keyNameAppGroup);
                } else {
                    appGroup = defaultAppGroup;
                }
            }
            // 2-2. HTTP Get 이외 - body에서 조회
            else {
                InputStream inputStream = request.getInputStream();
                byte[] rawData = IOUtils.toByteArray(inputStream);
                String bodyParameter = new String(rawData, StandardCharsets.UTF_8);
                JSONObject bodyParam = new JSONObject(bodyParameter);

                if(bodyParam.has(keyNameAppGroup)){
                    appGroup = bodyParam.getString(keyNameAppGroup);
                }
                if(bodyParam.has(keyNameDeviceKey)){
                    deviceKey = bodyParam.getString(keyNameDeviceKey);
                }
            }
        }

        // 2-3. deviceKey, appGroup 조건 불충분 시 실패 처리
        if ("".equals(deviceKey) || deviceKey == null || "null".equals(deviceKey)) {
            log.error("SignKeyCheck :: you don't have deviceKey");
            throw new CommonException.DefaultParameterException("you don't have deviceKey");
        }
        if ("".equals(certiKey) || certiKey == null || "null".equals(certiKey)) {
            log.error("SignKeyCheck :: you don't have certiKey");
            throw new CommonException.DefaultParameterException("you don't have certiKey");
        }

        // 3. certikey, devicekey 기준으로 useridx 조회
        String useridx = redisClusterKeyUtil.getAcntUserIdxByCertiKey(appGroup, certiKey);
        String myDeviceKey = redisClusterKeyUtil.getAcntDeviceKeyByCertiKey(appGroup, certiKey);

        // 요청한 certikey 가 맞지 않음.
        if (null == useridx || "".equals(useridx)) {
            log.error("SignKeyCheck :: useridx does not match");
            throw new CommonException.DefaultParameterException("useridx does not match");
        }

        // 4. 계정의 idType 확인 및 제한
        String id_type = redisClusterKeyUtil.getAcntIdTypeByUserIdx(useridx);
        // 요청한 deviceKey 가 같지 않다면 client 에서 로그 아웃됨
        if (id_type == null) {
            log.error("SignKeyCheck :: id_type does not exist");
            throw new CommonException.DefaultParameterException("id_type does not exist");
        }
        if (!myDeviceKey.equals(deviceKey) && !("5".equals(id_type) || "6".equals(id_type))) {
            log.error("SignKeyCheck :: deviceKey does not match");
            throw new CommonException.DefaultParameterException("deviceKey does not match");
        }

        request.setAttribute("userIdx", useridx);

        // 5. APM 저장
        String runningVersion = new VersionHelperUtil().getRunningVersion(servletConfig);// current version
        Transaction transaction = ElasticApm.currentTransaction();
        transaction.setLabel("certiKey", certiKey);
        transaction.setLabel("deviceKey", deviceKey);
        transaction.setLabel("appGroup", appGroup);
        transaction.setLabel("useridx", useridx);
        transaction.setLabel("server_version", runningVersion);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object object, Exception ex) throws Exception {

    }

}
