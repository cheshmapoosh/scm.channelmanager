package ir.daneshrefah.scm.uaa.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
@Data
@ConfigurationProperties(prefix = "scm.otp")
@Component
public class OtpProperties {

    private Integer timeToLiveMinutes;

}
