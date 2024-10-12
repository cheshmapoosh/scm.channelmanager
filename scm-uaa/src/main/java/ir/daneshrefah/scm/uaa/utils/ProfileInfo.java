package ir.daneshrefah.scm.uaa.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProfileInfo {

    private static final String DEVELOPMENT_PROFILE = "dev";
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

    public boolean isTraceMode() {
        return getActiveProfiles()
                .stream()
                .anyMatch(profile -> profile.equals(DEVELOPMENT_PROFILE));
    }

}
