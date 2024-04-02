package ir.daneshrefah.scm.plugin.hps.component;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
@Component("hps")
public class HpsComponent extends DefaultComponent {

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        HpsEndpoint endpoint = new HpsEndpoint();
        return endpoint;
    }

}
