package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.common.service.ServiceService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
public class ScmObjectModule extends SimpleModule {

    public ScmObjectModule(ServiceService service) {
        addSerializer(ServiceImplementationType.class, ServiceImplementationTypeSerializer.INSTANT);
        addSerializer(ServiceType.class, ServiceTypeSerializer.INSTANT);

        addDeserializer(Service.class, new ServiceDeserializer(service));
        addDeserializer(ServiceImplementationType.class, ServiceImplementationTypeDeserializer.INSTANT);
        addDeserializer(ServiceType.class, ServiceTypeDeserializer.INSTANT);
    }
}
