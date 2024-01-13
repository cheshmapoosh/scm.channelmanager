package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.person.ServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.ServiceAccessMapper;
import ir.daneshrefah.scm.core.model.person.Customer;
import ir.daneshrefah.scm.core.model.person.PersonProfile;
import ir.daneshrefah.scm.core.model.person.ServiceAccess;
import ir.daneshrefah.scm.core.repository.ServiceAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@RequiredArgsConstructor
@Service
public class PersonProfileService {

    private final ServiceAccessRepository serviceAccessRepository;

    public PersonProfile findPersonProfileById(String id) {
        PersonProfile personProfile = PersonProfile.builder()
                .customers(findCustomerListByPersonProfileId(id))
                .serviceAccesses(findServiceAccessListByPersonProfileId(id))
                .build();
        personProfile.setId(id);
        return personProfile;
    }

    private List<ServiceAccess> findServiceAccessListByPersonProfileId(String id) {
        List<ServiceAccessEntity> serviceAccessEntities = serviceAccessRepository.findByPersonProfileId(id);
        return ServiceAccessMapper.INSTANCE.entitiesToModels(serviceAccessEntities);
    }

    private List<Customer> findCustomerListByPersonProfileId(String id) {
//        private ExternalServiceProvider provider;
//        private String customerNo;
//        private List<Asset> assets;
        return null;
    }

}
