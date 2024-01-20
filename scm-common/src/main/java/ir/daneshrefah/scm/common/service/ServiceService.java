package ir.daneshrefah.scm.common.service;

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

    public List<Service> findServiceList();

    public Service findServiceByCode(String code);

    public Service createService(Service service);

    public Service updateService(String serviceId, Service service);

    public boolean checkServiceExistById(String serviceId);

    public boolean checkServiceProviderExistById(String serviceProviderId);

}
