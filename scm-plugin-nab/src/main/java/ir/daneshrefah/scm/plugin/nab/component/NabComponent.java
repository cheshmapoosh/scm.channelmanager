package ir.daneshrefah.scm.plugin.nab.component;

import ir.daneshrefah.scm.plugin.api.component.AbstractComponent;
import ir.daneshrefah.scm.plugin.api.component.AbstractEndpoint;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class NabComponent extends AbstractComponent {

    public NabComponent(String metadata) {
        super(metadata);
    }

    @Override
    protected AbstractEndpoint createInternalEndpoint(String uri, String remaining, Map<String, Object> parameters) {
        AbstractEndpoint endpoint = new NabEndpoint(uri, this);
        return endpoint;
    }
}
