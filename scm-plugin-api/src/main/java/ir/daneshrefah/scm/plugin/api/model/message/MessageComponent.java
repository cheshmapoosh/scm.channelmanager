package ir.daneshrefah.scm.plugin.api.model.message;

import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-25
 */
public class MessageComponent {

    private ServiceComponent serviceComponent;
    private Object payload;

    public ServiceComponent getServiceComponent() {
        return serviceComponent;
    }

    public void setServiceComponent(ServiceComponent serviceComponent) {
        this.serviceComponent = serviceComponent;
    }

    public <T> T getPayload(Class<T> type) {
        if (type.isInstance(payload)) {
            return (T) payload;
        }
        return null;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
