package ir.daneshrefah.scm.uaa.captcha;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.oopsguy.kaptcha.autoconfigure.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Properties;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-11
 *
 * https://github.com/oopsguy/kaptcha-spring-boot/blob/master/kaptcha-spring-boot-autoconfigure/src/main/java/com/oopsguy/kaptcha/autoconfigure/KaptchaAutoConfigure.java
 *
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfigure {

    @Bean(name = "captchaProps")
    protected Properties captchaProps(CaptchaProperties captchaProperties) {
        return ConfigUtils.kaptchaPropertiesToProperties(captchaProperties);
    }

    @Bean
    @ConditionalOnMissingBean(Producer.class)
    @DependsOn({"captchaProps"})
    public Producer defaultCaptcha(@Qualifier("captchaProps") Properties captchaProps) {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Config config = new Config(captchaProps);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

    @Bean
    @DependsOn({"captchaProps"})
    public ServletContextInitializer webConfig() {
        return new ServletRegisterInitializer();
    }

}
