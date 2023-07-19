package ir.daneshrefah.scm.core.config;

import ir.daneshrefah.scm.common.model.ApplicationSetting;
import ir.daneshrefah.scm.common.model.Profile;
import ir.daneshrefah.scm.service.ProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationSettingInitializer {

    @Value("${scm.profile}")
    private String activeProfileCode;
    private ProfileService profileService;

    public ApplicationSettingInitializer(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Bean
    public ApplicationSetting applicationSetting() {
        Profile profile = profileService.findProfileByName(activeProfileCode);

        ApplicationSetting applicationSetting = new ApplicationSetting();
        applicationSetting.setActiveProfile(profile);
        return applicationSetting;
    }

}