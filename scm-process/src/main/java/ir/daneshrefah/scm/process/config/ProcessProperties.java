package ir.daneshrefah.scm.process.config;

import ir.daneshrefah.scm.plugin.api.config.DatasourceProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-10
 */
//@ConfigurationProperties("scm.process") //TOOD uncomment
@Setter
@Getter
public class ProcessProperties {

    private boolean enabled;
    private DatasourceProperties datasource;
}
