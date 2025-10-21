package ir.daneshrefah.scm.uaa.service.proxy.spec;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ResponseProxyAdvisor {

    ResponseProxy<?> applyProxy(HttpServletRequest request, HttpServletResponse response,String responseBody);
    boolean support(HttpServletRequest request);
    String filterUrlPathPattern();

}

