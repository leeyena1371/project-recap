package co.project.api.common.filter;


import co.project.api.common.util.CompressorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestBodyDecryptWrapper extends HttpServletRequestWrapper {

    private byte[] decodingBody;
    private HttpServletRequest request;
    private String body;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public RequestBodyDecryptWrapper(HttpServletRequest request) throws Exception {
        super(request);
        this.request = request;

        // Convert InputStream data to byte array and store it to this wrapper instance.
        byte[] rawData;

        try {
            // GET 이외의 요청 일 경우 decompress 실행
            if (!request.getMethod().equals(RequestMethod.GET.name())) {

                // inputstream을 얻어온다
                InputStream inputStream = request.getInputStream();
                
                // inputstream을 읽어 요청 데이터를 가져온다. (byte)
                rawData = IOUtils.toByteArray(inputStream);

                // base64 decode and decompress
                CompressorUtil compressor = new CompressorUtil();
                byte[] decodeBytes = Base64.getMimeDecoder().decode(rawData);
                byte[] decompressdDecode = compressor.decompress(decodeBytes);

                body = new String(decompressdDecode, StandardCharsets.UTF_8);
//                log.info("[DECRYPT][DECODED_REQUEST] - {}", body);

                this.decodingBody = decompressdDecode;
            }
        } catch (Exception e) {
            log.error("[DECRYPT] - REQUEST INPUT STREAM READ FAIL.");
            log.error(e.getMessage(), e);
        }

        // 공통 request log
        printRequestLog();

    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodingBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    /**
     * 공통 reqeust log
     */
    private void printRequestLog() {

        log.info("----------------------------------------------------------------");
        Map<String, String> metaData = new HashMap<>();
        JSONObject headers = new JSONObject();

        Enumeration<String> headername = request.getHeaderNames();
        while (headername.hasMoreElements()) {
            String name = headername.nextElement();
            headers.put(name, request.getHeader(name));
        }
        request.getContentType();
        metaData.put("client", request.getRemoteHost());
        metaData.put("content-type", request.getContentType());
        metaData.put("headers", headers.toString());

        log.info("Path: {} {}", request.getMethod(), request.getRequestURI());
        log.info("Authorization: {}, Query string: {}", request.getHeader("Authorization"), request.getQueryString());
//        log.info("META: {}", metaData);
        log.info("body: {}", body);
    }

}
