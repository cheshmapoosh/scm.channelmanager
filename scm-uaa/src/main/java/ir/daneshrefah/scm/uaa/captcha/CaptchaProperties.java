package ir.daneshrefah.scm.uaa.captcha;

import com.oopsguy.kaptcha.autoconfigure.BaseProperties;
import com.oopsguy.kaptcha.autoconfigure.KaptchaProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-11
 */
@ConfigurationProperties(
        prefix = "scm.captcha"
)
public class CaptchaProperties extends BaseProperties {

    @Getter
    @Setter
    private Map<String, KaptchaProperties.SingleKaptchaProperties> items = new HashMap<>();

}
