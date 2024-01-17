package ir.daneshrefah.scm.plugin.scm.service;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
@RequiredArgsConstructor
@Service
public class ServiceManagementService extends AbstractJavaService {

    private final ServiceService service;

    public Object serviceList(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        return this.service.findServiceList();
    }

    public Object findServiceByCode(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        String serviceCode = message.getPayloadValue("serviceCode");
        if (StringUtils.isEmpty(serviceCode)) {
            return null;
        }
        return this.service.findServiceByCode(serviceCode);
    }

}
