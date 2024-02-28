package ir.daneshrefah.scm.uaa.repository.authentication;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Integer>, JpaSpecificationExecutor<RoleEntity> {

    @Query(value = "select r.* from REF.USERROLE ur " +
            "inner join REF.ROLE r on r.ROLE_ID = ur.ROLE_ID " +
            "where USER_ID = :personId ", nativeQuery = true)
    List<RoleEntity> findByPersonId(@Param("personId") Long personId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO userrole (USER_ID, ROLE_ID) VALUES (:personId, :roleId)")
    void insertPersonRole(@Param("personId") Long personId, @Param("roleId") Integer roleId);

    Optional<RoleEntity> findByCode(String code);
    void deleteRoleEntityByCode(String code);
}
