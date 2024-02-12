package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    @Column(name = "OTP_SERIAL_NO")
    private String otpSerialNumber;
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private GeneralPersonEntity person;
    @Column(name = "BRANCH_CODE")
    private String creatorBranch;
    @Column(name = "CREATED_BY")
    private String creator;
    @Column(name = "MODIFIED_BY")
    private String lastEditor;
    @Column(name = "CREATION_DATE")
    private LocalDateTime createDate;
    @Column(name = "MODIFICATION_DATE")
    private LocalDateTime lastEditDate;



//ARCHIVE_NO smallint NOT NULL DEFAULT '8',
//USER_AUTHENTICATION_TYPE => 1:UserChannelAuthentication, 2:EmployeeAuthentication
//FROM_DATE datetime(6) DEFAULT NULL,
//TO_DATE datetime(6) DEFAULT NULL,
//PRINT_COUNT smallint NOT NULL DEFAULT '0',
//PASSWORD_SET_PRINTED smallint DEFAULT NULL,
//EFFECTIVE_DATE datetime(6) DEFAULT NULL,
//STATE smallint NOT NULL DEFAULT '1',
//PIN_BASED_PASSWORD varchar(128) DEFAULT NULL,
//PATTERN_BASED_PASSWORD varchar(128) DEFAULT NULL,
//LAST_DATE_OF_PASSWORD_CHANGE datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
//LAST_REACTION_DATE_TO_PASSWORD datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

}
