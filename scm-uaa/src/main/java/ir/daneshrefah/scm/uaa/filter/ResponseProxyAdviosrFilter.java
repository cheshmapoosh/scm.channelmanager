package ir.daneshrefah.scm.uaa.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.uaa.security.token.OAuth2ShahkarAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.proxy.spec.ResponseProxy;
import ir.daneshrefah.scm.uaa.service.proxy.spec.ResponseProxyAdvisor;
import ir.daneshrefah.scm.uaa.utils.CachedAccessToken;
import ir.daneshrefah.scm.utils.functional.safe.SafeProcess;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class ResponseProxyAdviosrFilter implements Filter {

    private final HazelcastInstance hazelcast;
    private final BeanFactory beanFactory;
    private static final Map<String, ResponseProxyAdvisor> allResponseProxyAdvisors = new HashMap<>();
    private ObjectMapper objectMapper;
    private final static String tokenCacheMap = "scm-uaa:refresh:token-cache";
    private static IMap<String, CachedAccessToken<OAuth2ShahkarAuthenticationToken>> map;
    private final Long sessionTTL; //is set from yml  inside @Bean

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        objectMapper = beanFactory.getBean(ObjectMapper.class);
        Map<String, ResponseProxyAdvisor> injectedResponseProxyAdvisors = ((ListableBeanFactory) beanFactory).getBeansOfType(ResponseProxyAdvisor.class);
        allResponseProxyAdvisors.putAll(injectedResponseProxyAdvisors);
        allResponseProxyAdvisors.values().forEach(p -> log.info(">>> RESPONSE PROXY ADVISORS '{}' HAS BEEN INITIALIZED SUCCESSFULLY", p));
        map = hazelcast.getMap(tokenCacheMap);
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
                    String responseBodyJson = objectMapper.writeValueAsString(responseProxy.getResponseBody());
                    log.info("response proxy applied. status={}, contentType={}",
                            responseProxy.getHttpStatusCode(), responseProxy.getContentType());
                    responseWrapper.getWriter().write(responseBodyJson);
                }).onFailure(exception -> {
                    log.error("response proxy failed: {}", safeMessage(exception));
                }));


        if((request.getAttribute("Verify") !=null && (Boolean) request.getAttribute("Verify")) || ((RequestFacade) request).getRequestURI().equals("/auth/refresh") ){
            JsonNode jsonNode = objectMapper.readTree( new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8));
            long now = System.currentTimeMillis();
            String refreshToken =jsonNode.get("refresh_token").asText();
            if(StringUtils.isNotEmpty(refreshToken)) {
                CachedAccessToken<OAuth2ShahkarAuthenticationToken> cachedAccessToken = new CachedAccessToken<>((OAuth2ShahkarAuthenticationToken) request.getAttribute("preAuthenticationInstance"),now+sessionTTL);
                map.set(refreshToken,cachedAccessToken , now+sessionTTL, TimeUnit.MILLISECONDS);
            }
        }else if(request.getAttribute("Verify") !=null && !(Boolean) request.getAttribute("Verify")) {
            responseWrapper.reset();
        }
        responseWrapper.copyBodyToResponse();
    }

    private String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return exception == null ? null : exception.getClass().getSimpleName();
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}
