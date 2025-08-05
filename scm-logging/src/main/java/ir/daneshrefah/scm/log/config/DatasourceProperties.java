package ir.daneshrefah.scm.log.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@Getter
@Setter
public class DatasourceProperties extends DataSourceProperties {
    private int maxConnection;
    private String defaultSchema;
    private Boolean enabled;
}