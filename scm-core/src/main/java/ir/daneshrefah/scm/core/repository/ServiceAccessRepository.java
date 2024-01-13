package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.person.ServiceAccessEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Repository
public interface ServiceAccessRepository extends CrudRepository<ServiceAccessEntity, Long> {

    public List<ServiceAccessEntity> findByPersonProfileId(String personProfileId);

}
