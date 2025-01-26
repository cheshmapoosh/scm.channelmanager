package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ScopeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Repository
public interface ScopeRepository extends CrudRepository<ScopeEntity, Long> {

}
