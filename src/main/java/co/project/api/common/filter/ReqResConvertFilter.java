package co.project.api.common.filter;


import co.dearu.live.api.common.util.CompressorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@WebFilter(urlPatterns = "/live/chat/*" )
public class ReqResConvertFilter implements Filter {

    // 사내 IP
//    private final String INTERNAL_IP_ADDRESS = "58.227.2.154";

    private final String[] INTERNAL_IP_ADDRESS_ARR = {"58.227.2.154", "52.199.60.224"};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        log.info("filter init!!!");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            // ELB에서 health check 하는 요청 로그 제외
            if ((req.getRequestURI().toLowerCase().equals("/health") || req.getRequestURI().toLowerCase().equals("/healthcheck"))
                    && req.getMethod().equals(RequestMethod.GET.name())) {
                res.setStatus(HttpStatus.OK.value());
                return;
            }
            // 버전확인
            else if (req.getRequestURI().toLowerCase().equals("/version") && req.getMethod().equals(RequestMethod.GET.name())) {
                res.setStatus(HttpStatus.OK.value());
                chain.doFilter(req, res);
                return;
            }
            // swagger 관련 url은 filter에서 제외
            else if (req.getRequestURI().startsWith("/swagger-ui")
                    || req.getRequestURI().startsWith("/v3/api-docs")) {

                String ip = req.getHeader("X-FORWARDED-FOR");
                if (ip == null)
                    ip = req.getRemoteAddr();

                // 사내 ip만 접근 허용
                boolean isSwaggerAvailable = false;
                for (int i = 0; i < INTERNAL_IP_ADDRESS_ARR.length; i++) {
                    if (ip.startsWith(INTERNAL_IP_ADDRESS_ARR[i])) {
                        isSwaggerAvailable = true;
                        break;
                    }
                }
                if(!isSwaggerAvailable){
                    if (request.getServerName().equals("localhost")) {
                        isSwaggerAvailable = true;
                    }
                }


                if (isSwaggerAvailable) {
                    chain.doFilter(req, res);
                } else {
                    res.setStatus(HttpStatus.NOT_FOUND.value());
                }

                return;
            }

            ResponseBodyEncryptWrapper resWrapper = new ResponseBodyEncryptWrapper(res);

            // 사내ip이고, swagger로부터 온 요청은 filter처리 안함
            String ip = req.getHeader("X-FORWARDED-FOR");
            if (ip == null)
                ip = req.getRemoteAddr();

            String referer = req.getHeader("referer");

            if (referer != null && referer.contains("swagger-ui")) {

                boolean isSwaggerAvailable = false;

                if (request.getServerName().equals("localhost")) {
                    isSwaggerAvailable = true;
                } else {
                    for (int i = 0; i < INTERNAL_IP_ADDRESS_ARR.length; i++) {
                        if (ip.startsWith(INTERNAL_IP_ADDRESS_ARR[i])) {
                            isSwaggerAvailable = true;
                        }
                    }
                }

                if (isSwaggerAvailable) {
                    log.info("swagger route");
                    RequestBodyWrapper reqNoDecWrapper = new RequestBodyWrapper(req);
                    chain.doFilter(reqNoDecWrapper, resWrapper);
                    res.getOutputStream().write(resWrapper.getResponseData().getBytes(StandardCharsets.UTF_8));
                    return;
                }
            }

            RequestBodyDecryptWrapper reqWrapper = new RequestBodyDecryptWrapper(req);

            if (req.getMethod().equals("GET")) {
                chain.doFilter(req, resWrapper);
            } else {
                chain.doFilter(reqWrapper, resWrapper);
            }

            // filter response 후처리
            CompressorUtil compressor = new CompressorUtil();

            String getResponseData = resWrapper.getResponseData();
            System.out.println("getResponseData :: "+ getResponseData);

            byte[] compressData = compressor.compress(resWrapper.getResponseData());

            res.getOutputStream().write(compressData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        log.info("filter destroy!!!");
    }
}
