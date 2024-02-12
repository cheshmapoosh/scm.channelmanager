package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.person.CorporatePersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.EmployeePersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
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
 * @since 2024-01-24
 */
@Repository
public interface PersonRepository extends CrudRepository<GeneralPersonEntity, Integer> {

    @Query("SELECT p FROM IndividualPersonEntity p WHERE p.nationalCode = :nationalCode")
    IndividualPersonEntity findIndividualPersonByNationalCode(@Param("nationalCode") String nationalCode);

    @Query("SELECT p FROM EmployeePersonEntity p WHERE p.nationalCode = :nationalCode")
    EmployeePersonEntity findEmployeePersonByNationalCode(@Param("nationalCode") String nationalCode);

    @Query("SELECT p FROM CorporatePersonEntity p WHERE p.nationalId = :nationalId AND p.subOrganizationId = :subOrganizationId")
    CorporatePersonEntity findCorporatePersonByNationalCode(@Param("nationalId") String nationalId, @Param("subOrganizationId") String subOrganizationId);

    List<GeneralPersonEntity> findPersonByUsername(String username);

}
