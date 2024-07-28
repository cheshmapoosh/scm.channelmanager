package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ProviderTerminalCoding;
import ir.daneshrefah.scm.common.model.service.Service;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
public interface ServiceService {

    List<AssetProvider> findAssetProviderList();

    AssetProvider findAssetProviderById(Integer id);

    Service findAssetProviderProviderServiceByAssetProviderId(Integer id);

    List<AbstractExternalServiceProvider> findServiceProviderList();

    AbstractExternalServiceProvider findServiceProviderById(String id);

    AbstractExternalServiceProvider findServiceProviderByCode(String id);

    AbstractExternalServiceProvider findServiceProviderByIdOrCode(String value);

    List<Service> findServiceList();

    PagedResponseData<Service> findServiceList(ServiceFindRequest request);

    Service findServiceByCode(String code);

    Service findServiceById(String id);

    Service createService(ServiceInfoRequest service);

    Service updateService(ServiceInfoEditRequest request);

    boolean checkServiceExistById(String serviceId);

    boolean checkServiceProviderExistById(String serviceProviderId);

    void deleteService(ServiceDeleteRequest request);

    PagedResponseData<TerminalServiceAccessAssignmentResponse> findAllServiceAccessOnTerminal(ServiceAccessFindRequest request);

    Optional<ProviderTerminalCoding> findProviderTerminalCoding(String terminalCode, String clientId, String providerCode);

}
