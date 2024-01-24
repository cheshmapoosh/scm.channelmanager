package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
@Repository
public interface PersonRepository extends CrudRepository<GeneralPersonEntity, Integer> {

}
