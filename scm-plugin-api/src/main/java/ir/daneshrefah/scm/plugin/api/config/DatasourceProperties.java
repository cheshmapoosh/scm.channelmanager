package ir.daneshrefah.scm.plugin.api.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-10
 */
@Data
public class DatasourceProperties extends DataSourceProperties {

    private int maxConnection;
    private String defaultSchema;

}
