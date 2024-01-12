package ir.daneshrefah.scm.uaa.repository.authentication;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Repository
public interface RoleRepository extends CrudRepository<RoleEntity,Long> {

    @Query(value = "select r.* from REF.USERROLE ur " +
            "inner join REF.ROLE r on r.ROLE_ID = ur.ROLE_ID " +
            "where USER_ID = :personId ", nativeQuery = true)
    List<RoleEntity> findByPersonId(@Param("personId") Long personId);

}
