package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.person.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
@Repository
public interface PersonRepository extends JpaRepository<GeneralPersonEntity, Long>, JpaSpecificationExecutor<GeneralPersonEntity> {

    @Query("SELECT p FROM GeneralRealPersonEntity p WHERE p.nationalCode = :nationalCode")
    GeneralRealPersonEntity findRealPersonByNationalCode(@Param("nationalCode") String nationalCode);

    @Query("SELECT p FROM IndividualPersonEntity p WHERE p.nationalCode = :nationalCode")
    IndividualPersonEntity findIndividualPersonByNationalCode(@Param("nationalCode") String nationalCode);

    @Query("SELECT p FROM EmployeePersonEntity p WHERE p.nationalCode = :nationalCode")
    EmployeePersonEntity findEmployeePersonByNationalCode(@Param("nationalCode") String nationalCode);

    @Query(value = "SELECT p.* FROM USER p " +
            "INNER JOIN USER_CHANNEL_AUTHENTICATION uca ON p.USER_ID = uca.USER_ID " +
            "WHERE uca.NICK_NAME = :nickname AND uca.CHANNEL_ID = :terminalId",
            nativeQuery = true)
    Optional<GeneralPersonEntity> findByNicknameAndTerminalId(@Param("nickname") String nickname, @Param("terminalId") Integer terminalId);

    List<GeneralPersonEntity> findPersonByUsername(String username);

    @Query("SELECT p FROM GeneralLegalPersonEntity p WHERE p.nationalId = :nationalId")
    GeneralLegalPersonEntity findGeneralLegalPersonEntityByNationalId(@Param("nationalId") String nationalId);

    @Query("SELECT p FROM GeneralLegalPersonEntity p WHERE p.nationalId = :nationalId and p.subOrganizationId = :subOrganizationId")
    GeneralLegalPersonEntity findGeneralLegalPersonEntityByNationalIdAndSubOrganizationId(@Param("nationalId") String nationalId, @Param("subOrganizationId") String subOrganizationId);


    @Query(value = """
            SELECT u.*
            FROM REF.USER u
            WHERE u.NATIONAL_CODE = :nationalCode
              AND u.USER_ID NOT IN (
                SELECT utd.user_id
                FROM REF.USER_TOKEN_DETAILS utd
                WHERE utd.TOKEN_TYPE IN (:tokenTypes)
            )""", nativeQuery = true)
    List<GeneralPersonEntity> findOtpRegistration(
            @Param("nationalCode") String nationalCode,
            @Param("tokenTypes") List<String> tokenTypes
    );

    @Query(value = """ 
            SELECT u.*
            FROM REF.USER u
            INNER JOIN REF.USER_TOKEN_DETAILS utd ON u.USER_ID = utd.USER_ID
            WHERE utd.TOKEN_TYPE IN (:tokenTypes)
            AND u.NATIONAL_CODE = :nationalCode""", nativeQuery = true)
    List<GeneralPersonEntity> findByTokenTypesAndNationalCode(
            @Param("nationalCode") String nationalCode,
            @Param("tokenTypes") List<String> tokenTypes
    );

    @Query(value = """ 
            SELECT u.*
            FROM REF.USER u
            INNER JOIN REF.USER_TOKEN_DETAILS utd ON u.USER_ID = utd.USER_ID
            WHERE utd.TOKEN_TYPE IN (:tokenTypes)
            AND u.NATIONAL_CODE = :nationalCode
            AND utd.SERIAL_NO = :otpSerialNo""", nativeQuery = true)
    List<GeneralPersonEntity> findByTokenTypesAndNationalCodeAndOtpSerialNo(
            @Param("nationalCode") String nationalCode,
            @Param("tokenTypes") List<String> tokenTypes,
            @Param("otpSerialNo") String otpSerialNo
    );
}
