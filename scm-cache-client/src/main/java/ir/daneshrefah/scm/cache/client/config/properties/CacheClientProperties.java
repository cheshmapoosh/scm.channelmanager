package ir.daneshrefah.scm.cache.client.config.properties;

import com.hazelcast.config.NearCacheConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author dariush abdolahi
 * @version 1.0
 * @since 2023-11-22
 */
@ConfigurationProperties("scm.cache.client.config")
@Setter
@Getter
public class CacheClientProperties {
    /*
       set properties on target module
       config sample >>

       scm:
         cache:
            client:
                config:
                    cluster-name: dev3
                    server-host: 127.0.0.1
                    server-port: 5701
                    near-cache-config:
                         myMap:
                            in-memory-format: object
                            time-to-live-seconds: 10

     */

    private String serverHost;                              //mandatory
    private String serverPort;                              //mandatory
    private String clusterName;                             //mandatory
    private Map<String, NearCacheConfig> nearCacheConfig;   //optional
}
