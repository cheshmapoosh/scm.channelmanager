package ir.daneshrefah.scm.uaa.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-29
 */
public class RequestLoggingFilter implements Filter {

    private static final Log LOGGER = LogFactory.getLog(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Instant startTime = Instant.now();
        chain.doFilter(request, response);
        Instant endTime = Instant.now();
        LOGGER.info(buildLogMessage((HttpServletRequest) request, (HttpServletResponse) response, startTime, endTime));
    }

    private Object buildLogMessage(HttpServletRequest request, HttpServletResponse response, Instant startTime, Instant endTime) {
        long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
        return String.format("[%s] %s %d ms - StartTime: %s, Status: %d, ContentType: %s",
                request.getMethod(), request.getRequestURI(), duration, startTime, response.getStatus(), request.getContentType());
    }

}
