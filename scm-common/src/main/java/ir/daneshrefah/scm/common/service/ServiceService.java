package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
public interface ServiceService {

    List<ExternalServiceProvider> findServiceProviderList();

    ExternalServiceProvider findServiceProviderById(String id);

    ExternalServiceProvider findServiceProviderByCode(String id);

    public List<Service> findServiceList();

    public List<Service> findServiceList(ServiceInfoRequest request);

    public Service findServiceByCode(String code);

    public Service findServiceById(String id);

    public Service createService(Service service);

    public Service updateService(String serviceId, Service service);

    public boolean checkServiceExistById(String serviceId);

    public boolean checkServiceProviderExistById(String serviceProviderId);

}
