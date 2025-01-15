package ir.daneshrefah.scm.uaa.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.TimeZone;

@Configuration
@Slf4j
@Order(Integer.MIN_VALUE)
public class TimeZoneConfiguration {

    private static final String IRAN_STANDARD_TIME_ZONE = "Asia/Tehran";

    @PostConstruct
    public void init() {
        // Set default time zone to Tehran
        TimeZone.setDefault(TimeZone.getTimeZone(IRAN_STANDARD_TIME_ZONE));
        log.info(">>> Default time zone set to: {}" , TimeZone.getDefault().getID());
    }
}
