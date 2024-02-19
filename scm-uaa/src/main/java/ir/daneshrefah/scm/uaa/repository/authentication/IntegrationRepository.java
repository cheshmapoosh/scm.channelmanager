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
public interface IntegrationRepository extends CrudRepository<UserEntity,Long> {

    @Query(value = "SELECT CODE AS \"key\", CHANNEL_ID AS value FROM REF.CHANNEL " +
            "WHERE PARENT_ID IS NULL AND ACTIVE = 1", nativeQuery = true)
    List<String[]> findAllTerminals();

    @Query(value = "SELECT USER_ID FROM REF.USER WHERE USERNAME = :personProfileId;", nativeQuery = true)
    Long findPersonIdByPersonProfileId(@Param("personProfileId") String personProfileId);

}
