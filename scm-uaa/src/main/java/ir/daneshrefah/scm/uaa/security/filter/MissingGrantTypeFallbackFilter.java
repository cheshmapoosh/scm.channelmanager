
package ir.daneshrefah.scm.uaa.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.utils.Urls;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_ACCESS_PARAMETER;

public class MissingGrantTypeFallbackFilter extends OncePerRequestFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        boolean isTokenEndpoint = request.getServletPath().equals(Urls.OAUTH2_TOKEN);
        boolean isPost = HttpMethod.POST.name().equalsIgnoreCase(request.getMethod());

        if (isTokenEndpoint && isPost && isMissing(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))) {
            ContentCachingRequestWrapper cachingRequestWrapper = new ContentCachingRequestWrapper(request);
            DefaultGrantTypeRequestWrapper wrappedRequest = new DefaultGrantTypeRequestWrapper(cachingRequestWrapper);
            chain.doFilter(wrappedRequest, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isMissing(String v) {
        return v == null || v.isBlank();
    }

    private static class DefaultGrantTypeRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> params = new HashMap<>();

        DefaultGrantTypeRequestWrapper(HttpServletRequest request) {
            super(request);
            params.putAll(request.getParameterMap());
            params.putIfAbsent(OAuth2ParameterNames.GRANT_TYPE, new String[]{AuthorizationGrantType.DEFAULT.getCode()});
            params.putIfAbsent(OAuth2ParameterNames.SCOPE, new String[]{"session"});
            if(request.getHeader("x-otp-code") !=null){
                params.putIfAbsent("x-otp-code",new String[]{request.getHeader("x-otp-code")});
            }
            enrichUserInfo(params, request);
            enrichIfExtShk(params, request);
        }

        @SuppressWarnings("unchecked")
        private void enrichUserInfo(Map<String, String[]> params, HttpServletRequest request) {
            try {
                Map<String, Object> bodyMap = objectMapper.readValue(request.getInputStream(), Map.class);
                bodyMap.computeIfPresent("username", (k, v) -> {
                    params.put(OAuth2ParameterNames.USERNAME, new String[]{String.valueOf(v)});
                    return v;
                });
                bodyMap.computeIfPresent("password", (k, v) -> {
                    params.put(OAuth2ParameterNames.PASSWORD, new String[]{String.valueOf(v)});
                    return v;
                });

            } catch (Exception ignore) {
            }
        }

        @Override
        public String getParameter(String name) {
            String[] v = params.get(name);
            return (v != null && v.length > 0) ? v[0] : super.getParameter(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(params);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(params.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            return params.getOrDefault(name, super.getParameterValues(name));
        }

        @SuppressWarnings("unchecked")
        private void enrichIfExtShk(Map<String, String[]> params, HttpServletRequest request) {

            if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
                return;
            }

            byte[] body = wrapper.getContentAsByteArray();
            if (body.length == 0) {
                return;
            }

            try {
                Map<String, Object> bodyMap =
                        objectMapper.readValue(body, Map.class);

                Object grantTypeObj = bodyMap.get(OAuth2ParameterNames.GRANT_TYPE);
                if (grantTypeObj == null) {
                    return; // ✅ سایر توکن‌ها untouched
                }

                String grantType = String.valueOf(grantTypeObj);

                // ✅ تنها نقطه دخالت
                if (!"ext_shk".equals(grantType)) {
                    return;
                }

                // ✅ override فقط برای ext_shk
                params.put(
                        OAuth2ParameterNames.GRANT_TYPE,
                        new String[]{grantType}
                );
                //request.   (SCM_PARAMETER_ACCESS_PARAMETER,new String[]{String.valueOf(bodyMap.get(OAuth2ParameterNames.PASSWORD))});

                if (bodyMap.containsKey(OAuth2ParameterNames.USERNAME)) {
                    params.put(
                            OAuth2ParameterNames.USERNAME,
                            new String[]{String.valueOf(bodyMap.get(OAuth2ParameterNames.USERNAME))}
                    );
                }

                if (bodyMap.containsKey(OAuth2ParameterNames.PASSWORD)) {
                    params.put(
                            OAuth2ParameterNames.PASSWORD,
                            new String[]{String.valueOf(bodyMap.get(OAuth2ParameterNames.PASSWORD))}
                    );
                }

            } catch (Exception ignored) {

            }
        }
    }

}
