package ir.daneshrefah.scm.migration.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlyWayConfig {

    @Value(value = "${flyway.baseline-on-migrate:false}")
    public boolean baselineOnMigration;
    @Value(value = "${flyway.validate-on-migrate:false}")
    public boolean validateOnMigrate;
    @Value(value = "${flyway.location:classpath:/db/migration}")
    public String location;
    @Value(value = "${flyway.baselineVersion:0}")
    public String baselineVersion;

    @Bean
    @ConditionalOnProperty(name = "flyway.enable", havingValue = "true")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(location)
                .createSchemas(true)
                .baselineOnMigrate(baselineOnMigration)
                .baselineVersion(baselineVersion)
                .validateOnMigrate(validateOnMigrate)
                .placeholderReplacement(false)
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    @ConditionalOnProperty(name = "flyway.enable", havingValue = "false", matchIfMissing = true)
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // do nothing
        };
    }

}
