package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Getter
@Setter
@Entity
@Table(name = "USER_CHANNEL_AUTHENTICATION")
public class UserEntity extends AbstractEntity<Integer> {

    @Id
    @Column(name = "USER_CHANNEL_AUTHENTICATION_ID")
    private Integer id;
    @Column(name = "NICK_NAME")
    private String nickname;
    @Column(name = "CHANNEL_ID")
    private Integer terminalId;
    @Column(name = "AUTHENTICATION_METHOD_ID")
    @Convert(converter = AuthenticationMethodConverter.class)
    private AuthenticationMethod loginAuthenticationMethod;
    @Column(name = "SECOND_LEVEL_AUTH_METHOD_ID")
    @Convert(converter = AuthenticationMethodConverter.class)
    private AuthenticationMethod transactionAuthenticationMethod;
    @Column(name = "CHANNEL_ACCESS_PARAM")
    @Convert(converter = AccessParameterConverter.class)
    private Set<String> accessParameters;
    private Boolean active;
    @Column(name = "FIRST_PASSWORD")
    private String loginStaticPassword;
    @Column(name = "SECOND_PASSWORD")
    private String transactionStaticPassword;
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private GeneralPersonEntity person;
//    ARCHIVE_NO
//    ACTIVE
//    USER_AUTHENTICATION_TYPE
//    USER_ID
//    FROM_DATE
//    TO_DATE
//    FIRST_PASSWORD
//    SECOND_PASSWORD
//    CHANNEL_ACCESS_PARAM
//    PRINT_COUNT
//    PASSWORD_SET_PRINTED
//    CREATED_BY
//    MODIFIED_BY
//    CREATION_DATE
//    MODIFICATION_DATE
//    EFFECTIVE_DATE
//    OTP_SERIAL_NO
//    STATE
//    BRANCH_CODE
//    PIN_BASED_PASSWORD
//    PATTERN_BASED_PASSWORD
//    LAST_DATE_OF_PASSWORD_CHANGE
//    LAST_REACTION_DATE_TO_PASSWORD

}
