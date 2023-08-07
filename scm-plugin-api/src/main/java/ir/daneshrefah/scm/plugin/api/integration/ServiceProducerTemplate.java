package ir.daneshrefah.scm.plugin.api.integration;

import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public interface ServiceProducerTemplate {

    public void callService(Service service, Message message);

}
