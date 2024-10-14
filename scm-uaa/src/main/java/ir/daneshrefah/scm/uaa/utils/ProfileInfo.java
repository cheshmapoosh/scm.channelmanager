package ir.daneshrefah.scm.uaa.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProfileInfo {

    private static final String DEVELOPMENT_PROFILE = "dev";
    private static Boolean TRACE_MODE_STATUS = null;
    private final Environment environment;

    public String getCurrentProfile() {
        List<String> activeProfiles = getActiveProfiles();
        if (!activeProfiles.isEmpty()) {
            return activeProfiles.get(0);
        } else {
            return "default";
        }
    }

    public List<String> getActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.stream(activeProfiles).toList();
    }

    /**
     * This status used for logging business detail or any tracing activity on development environment.
     */
    public boolean isTraceMode() {
        if (Objects.isNull(TRACE_MODE_STATUS)) {
            synchronized (this) {
                if (Objects.nonNull(TRACE_MODE_STATUS)) {
                    TRACE_MODE_STATUS = getActiveProfiles()
                            .stream()
                            .anyMatch(profile -> profile.equals(DEVELOPMENT_PROFILE));
                }
            }
        }
        return TRACE_MODE_STATUS;
    }

}
