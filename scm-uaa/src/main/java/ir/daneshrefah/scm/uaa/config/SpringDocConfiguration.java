package ir.daneshrefah.scm.uaa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"default","dev","test","prod"})
public class SpringDocConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme customAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("customAuth", customAuthScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList("customAuth"));
    }

}
