package ir.daneshrefah.scm.plugin.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-10
 */
@Getter
@Setter
public class DatasourceProperties extends DataSourceProperties {

    private int maxConnection;
    private String defaultSchema;

}
