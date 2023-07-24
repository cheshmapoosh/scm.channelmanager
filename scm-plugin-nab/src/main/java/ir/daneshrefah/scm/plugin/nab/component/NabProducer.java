package ir.daneshrefah.scm.plugin.nab.component;

import ir.daneshrefah.scm.plugin.api.component.AbstractEndpoint;
import ir.daneshrefah.scm.plugin.api.component.AbstractProducer;
import ir.daneshrefah.scm.plugin.api.model.message.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class NabProducer extends AbstractProducer {

    public NabProducer(AbstractEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public Object internalProcess(Message message) throws Exception {
        return "Test Nab ";
    }
}
