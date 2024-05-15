package ir.daneshrefah.scm.uaa.repository.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface UserRepository extends JpaRepository<UserEntity, Integer>, JpaSpecificationExecutor<UserEntity> {

    @Query(value = "SELECT u FROM UserEntity u " +
            "WHERE u.nickname = :nickname AND u.terminalId = :terminalId ")
    List<UserEntity> findByNicknameAndTerminalId(@Param("nickname") String nickname, @Param("terminalId") Integer terminalId);

//    @Query(value = "select UA.*, FAM.CODE AS LOGIN_AUTHENTICATION_METHOD, SAM.CODE AS TRANSACTION_AUTHENTICATION_METHOD " +
//            "FROM REF.USER_CHANNEL_AUTHENTICATION UA " +
//            "JOIN REF.AUTHENTICATION_METHOD FAM ON UA.AUTHENTICATION_METHOD_ID = FAM.AUTHENTICATION_METHOD_ID " +
//            "JOIN REF.AUTHENTICATION_METHOD SAM ON UA.SECOND_LEVEL_AUTH_METHOD_ID = SAM.AUTHENTICATION_METHOD_ID " +
//            "WHERE UA.NICK_NAME = :username AND UA.CHANNEL_ID = :terminalId ", nativeQuery = true)
//    List<UserEntity> findByUsernameAndTerminalCode(@Param("username") String username, @Param("terminalId") Integer terminalId);

}
