package ir.daneshrefah.scm.uaa.filter;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static ir.daneshrefah.scm.uaa.utils.Constants.REQUEST_ATTRIBUTE_CORRELATION_ID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-29
 */
public class CorrelationIdPreProcessingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (Objects.isNull(request.getAttribute(REQUEST_ATTRIBUTE_CORRELATION_ID))) {
            request.setAttribute(REQUEST_ATTRIBUTE_CORRELATION_ID, UUID.randomUUID().toString());
        }
        chain.doFilter(request, response);
    }

}
