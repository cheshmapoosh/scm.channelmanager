package ir.daneshrefah.scm.uaa.client.autoconfigure;

import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Configuration
public class AuthenticationClientAutoConfiguration {


    @Bean
    public AuthenticationClientTemplate authenticationClientTemplate() {
        return new AuthenticationClientTemplate();
    }

}
