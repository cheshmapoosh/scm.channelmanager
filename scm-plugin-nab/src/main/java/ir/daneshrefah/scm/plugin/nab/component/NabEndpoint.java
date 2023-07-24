package ir.daneshrefah.scm.plugin.nab.component;

import ir.daneshrefah.scm.plugin.api.component.AbstractComponent;
import ir.daneshrefah.scm.plugin.api.component.AbstractEndpoint;
import ir.daneshrefah.scm.plugin.api.component.AbstractProducer;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class NabEndpoint extends AbstractEndpoint {

    public NabEndpoint(String endpointUri, AbstractComponent component) {
        super(endpointUri, component);
    }

    @Override
    public AbstractProducer createInternalProducer() throws Exception {
        return new NabProducer(this);
    }
}
