package ir.daneshrefah.scm.core.services;


import ir.daneshrefah.scm.common.model.customer.ServiceAccess;
import ir.daneshrefah.scm.core.entity.person.ServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.ServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.ServiceAccessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceAccessService {

    private final ServiceAccessRepository serviceAccessRepository;

    public List<ServiceAccess> findByPersonUsername(String personUsername) {
        List<ServiceAccessEntity> entities = serviceAccessRepository.findByPersonProfileId(personUsername);
        return ServiceAccessMapper.INSTANCE.entitiesToModels(entities);
    }

}
