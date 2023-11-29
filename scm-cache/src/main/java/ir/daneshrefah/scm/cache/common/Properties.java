package ir.daneshrefah.scm.cache.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Properties {

    //Datasource config
    @Value("${spring.datasource.url}")
    public String datasourceUrl;
    @Value("${spring.datasource.driver-class-name}")
    public String datasourceDriverClassName;
    @Value("${spring.datasource.username}")
    public String datasourceUsername;
    @Value("${spring.datasource.password}")
    public String datasourcePassword;

}
