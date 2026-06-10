package ir.daneshrefah.scm.observation.web;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class ObservationWebMvcConfigurer implements WebMvcConfigurer {
    private final ObservationWebMvcInterceptor interceptor;

    public ObservationWebMvcConfigurer(ObservationWebMvcInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
