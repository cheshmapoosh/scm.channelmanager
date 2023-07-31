package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.plugin.api.model.message.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-31
 */
public interface ServiceComponentExecutor {

    public void executeServiceComponent(String serviceComponentProviderCode, String serviceComponentCode,
                                        Message message, Object payload);

}
