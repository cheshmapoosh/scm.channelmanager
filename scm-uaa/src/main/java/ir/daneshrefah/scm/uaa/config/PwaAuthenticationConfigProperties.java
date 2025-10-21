package ir.daneshrefah.scm.uaa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.security.oauth.config.pwa")
public class PwaAuthenticationConfigProperties {

    private Boolean whiteListEnabled;
    private ActivationConfig activation;
    private LoginConfig login;

    public record ActivationConfig(
            Integer rateLimitCount,
            Integer rateLimitBlockedTimeMinutes,
            Boolean checkRegistryToken,
            Integer otpCodeExpirationMinutes,
            Integer otpCodeTrailsCount){}

    public record LoginConfig(
            Integer rateLimitCount,
            Integer rateLimitBlockedTimeMinutes,
            Integer tokenChangeWarnPeriodDays
    ){}


}
