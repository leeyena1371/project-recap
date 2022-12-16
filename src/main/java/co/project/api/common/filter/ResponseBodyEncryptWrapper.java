package co.project.api.common.filter;


import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseBodyEncryptWrapper extends HttpServletResponseWrapper {
    ByteArrayOutputStream output;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response The response to be wrapped
     * @throws IllegalArgumentException if the response is null
     */
    public ResponseBodyEncryptWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {

        ServletOutputStream outputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener listener) {

            }

            @Override
            public void write(int b) throws IOException {
                output.write(b);
            }
        };
        return outputStream;
    }

    public byte[] getDataStream() {
        return output.toByteArray();
    }

    /**
     * response body에 APM에서 발행된 traceId를 삽입
     * @return
     */
    public String getResponseData() {

        String result = new String(getDataStream(), StandardCharsets.UTF_8);

        Transaction transaction = null;
        try {
            transaction = ElasticApm.currentTransaction();

            JSONObject json =  new JSONObject(result);
            json.put("traceId", transaction.getTraceId());

            // APM에 response body 추가
            transaction.addCustomContext("response_body", json.toString());

            result = json.toString();
        } catch (Exception e) {
            if(transaction != null) transaction.addCustomContext("response_body", result);
        }
        log.info("[RESPONSE][ENCRYPT][BEFORE] - Status : {}, Data : {}", getStatus(), result);

        return result;
    }
}