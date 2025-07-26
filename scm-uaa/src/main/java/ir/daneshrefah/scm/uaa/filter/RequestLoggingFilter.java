package ir.daneshrefah.scm.uaa.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_FORM;
import static ir.daneshrefah.scm.utils.string.StringUtils.isBlank;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-29
 */
public class RequestLoggingFilter implements Filter {

    private static final int MAX_BODY_LENGTH = 1024;
    private static final Log LOGGER = LogFactory.getLog(RequestLoggingFilter.class);
    private final List<String> unAuthorizedKeyList = List.of("password", "pass", "pw");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Instant startTime = Instant.now();
        chain.doFilter(request, response);
        Instant endTime = Instant.now();
        LOGGER.info(buildLogMessage((HttpServletRequest) request, (HttpServletResponse) response, startTime, endTime));
    }

    private Object buildLogMessage(HttpServletRequest request, HttpServletResponse response, Instant startTime, Instant endTime) {
        long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
        String requestBody = extractRequestBody(request);
        String responseBody = extractResponseBody(response);
        return String.format("[%s] %s %d ms - StartTime: %s, Status: %d, RequestBody: %s, RequestContentType: %s",
                request.getMethod(), request.getRequestURL(), duration, startTime, response.getStatus(), requestBody, request.getContentType());
    }


    private String extractRequestBody(ServletRequest request) {
        String contentType = request.getContentType();
        if (isBlank(contentType)) {
            return null;
        }
        if (HTTP_HEADER_CONTENT_TYPE_FORM.equals(contentType)) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();

                for (int i = 0; i < values.length; i++) {
                    if (!unAuthorizedKeyList.contains(key.toLowerCase())) {
                        sb.append(key).append("=").append(values[i]).append("&");
                    } else {
                        sb.append(key).append("=").append("***").append("&");
                    }
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.substring(0, Math.min(sb.length(), MAX_BODY_LENGTH));
        }
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.substring(0, Math.min(sb.length(), MAX_BODY_LENGTH));
        } catch (IOException e) {
            LOGGER.error("error extract Request Body.", e);
            return e.getMessage();
        }
    }

    private String extractResponseBody(ServletResponse response) {
        if (response.getContentType() != null) {
//            ServletOutputStream outputStream = response.getOutputStream();
//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//            int bytesRead;
//            byte[] chunk = new byte[1024];
//            while ((bytesRead = outputStream.rea(chunk)) > 0) {
//
//            }
        } else {
            return "Response body unavailable.";
        }
        return null;
    }

}
