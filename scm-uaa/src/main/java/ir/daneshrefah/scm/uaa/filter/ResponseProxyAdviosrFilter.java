package ir.daneshrefah.scm.uaa.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.service.proxy.spec.ResponseProxy;
import ir.daneshrefah.scm.uaa.service.proxy.spec.ResponseProxyAdvisor;
import ir.daneshrefah.scm.utils.functional.safe.SafeProcess;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class ResponseProxyAdviosrFilter implements Filter {

    private final BeanFactory beanFactory;
    private static final Map<String, ResponseProxyAdvisor> allResponseProxyAdvisors = new HashMap<>();
    private ObjectMapper objectMapper;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        objectMapper = beanFactory.getBean(ObjectMapper.class);
        Map<String, ResponseProxyAdvisor> injectedResponseProxyAdvisors = ((ListableBeanFactory) beanFactory).getBeansOfType(ResponseProxyAdvisor.class);
        allResponseProxyAdvisors.putAll(injectedResponseProxyAdvisors);
        allResponseProxyAdvisors.values().forEach(p -> log.info(">>> RESPONSE PROXY ADVISORS '{}' HAS BEEN INITIALIZED SUCCESSFULLY", p));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, responseWrapper);
        allResponseProxyAdvisors
                .values()
                .stream()
                .filter(ignore -> request instanceof HttpServletRequest)
                .filter(proxy -> Objects.nonNull(proxy.filterUrlPathPattern()))
                .filter(proxy -> ((HttpServletRequest) request).getServletPath().toLowerCase().startsWith(proxy.filterUrlPathPattern().toLowerCase()))
                .filter(proxy -> proxy.support((HttpServletRequest) request))
                .forEach(proxy -> SafeProcess.of().tryRun(() -> {
                    byte[] responseArray = responseWrapper.getContentAsByteArray();
                    String responseBody = new String(responseArray, response.getCharacterEncoding());
                    ResponseProxy<?> responseProxy = proxy.applyProxy((HttpServletRequest) request,responseWrapper,responseBody);
                    responseWrapper.resetBuffer();
                    responseWrapper.setContentType(responseProxy.getContentType());
                    responseWrapper.setStatus(responseProxy.getHttpStatusCode());
                    responseWrapper.getWriter().write(objectMapper.writeValueAsString(responseProxy.getResponseBody()));
                }).onFailure(exception -> {
                    log.error(exception.getMessage(), exception);
                }));
        responseWrapper.copyBodyToResponse();
    }
}
