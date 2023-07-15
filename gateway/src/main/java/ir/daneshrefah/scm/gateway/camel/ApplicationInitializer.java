package ir.daneshrefah.scm.gateway.camel;


import ir.daneshrefah.scm.common.model.ProfileEntity;
import ir.daneshrefah.scm.gateway.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationInitializer/* implements ApplicationRunner*/ {

    @Value("${scm.profile}")
    private String activeProfileCode;
    private ProfileRepository profileRepository;


    public ApplicationInitializer(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        activeProfile = profileRepository.findByName(activeProfileCode);
//    }

    @Bean
    public ProfileEntity activeProfile() {
        return profileRepository.findByName(activeProfileCode);
    }

    @Bean
    public String activeProfileCode() {
        return activeProfileCode;
    }

    @Bean
    public String activeProfileId(ProfileEntity activeProfile) {
        return null != activeProfile ? activeProfile.getId() : null;
    }

}
