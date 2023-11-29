package ir.daneshrefah.scm.cache.client.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description of the class or purpose of the file.
 *
 * @author dariush abdolahi
 * @version 1.0
 * @since 2023-11-22
 */
@ConfigurationProperties("cache.config.client")
@Setter
@Getter
public class HazelcastClientProperties {
    /*
       set properties on target module
       for example :
       cache.config.client.server-host : 127.0.0.1

       config sample >>
                        cache:
                            config:
                              client:
                                server-host: 127.0.0.1
                                server-port: 5701
                                cluster-name: dev3
                                near-cache-enabled: true

     */
    private String serverHost;          //mandatory
    private String serverPort;          //mandatory
    private String clusterName;         //mandatory
    private boolean nearCacheEnabled;   //optional
}
