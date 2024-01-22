package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-22
 */
public class ServiceEntityFactory {

    public static ServiceEntity createEmptyServiceEntity(String serviceId, ServiceImplementationType implementationType) {
        ServiceEntity result = null;
        switch (implementationType) {
            case EXTERNAL:
                result = new ExternalServiceEntity();
                break;
            case JAVA:
                result = new JavaServiceEntity();
                break;
            case COMPOSITION:
                result = new CompositionServiceEntity();
                break;
            case PARENT:
                result = new ParentServiceEntity();
                break;
            case BPMN:
                return null;
        }
        result.setId(serviceId);
        return result;
    }
}
