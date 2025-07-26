package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.common.data.converter.UserStatusConverter;
import ir.daneshrefah.scm.common.data.converter.UserTypeConverter;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.model.person.UserStatus;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.user.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
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
@SequenceGenerator(name = "ucaGenerator",allocationSize = 1,schema = "REF",sequenceName = "SQUSERCHANNELAUTHENTICATION")
public class UserEntity extends AbstractDefaultAuditableEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "ucaGenerator")
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
    @Column(name = "ACTIVE")
    @Convert(converter = UserStatusConverter.class)
    private UserStatus status;
    @Column(name = "FIRST_PASSWORD")
    private String loginStaticPassword;
    @Column(name = "SECOND_PASSWORD")
    private String transactionStaticPassword;
    @Column(name = "OTP_SERIAL_NO")
    private String otpSerialNumber;
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private GeneralPersonEntity person;
    @Column(name = "USER_AUTHENTICATION_TYPE")
    @Convert(converter = UserTypeConverter.class)
    private UserType type;
    @Column(name = "BRANCH_CODE")
    private String creatorBranch;
    @Column(name = "CREATED_BY")
    private Integer creator;
    @Column(name = "MODIFIED_BY")
    private Integer lastEditor;
    @Column(name = "CREATION_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;
    @Column(name = "MODIFICATION_DATE", insertable = false)
    private LocalDateTime lastEditDate;
    @Column(name = "PRINT_COUNT")
    private Integer printCount;
    @Column(name = "LAST_DATE_OF_PASSWORD_CHANGE")
    private LocalDate lastDateOfFirstPasswordChange;
    @Column(name = "LAST_REACTION_DATE_TO_PASSWORD")
    private LocalDate lastReactionDateToFirstPasswordChange;
    private LocalDate lastReactionDateToFirstPasswordChange;

    @PrePersist
    public void prePersist() {
        if (Objects.isNull(lastDateOfFirstPasswordChange)) {
            lastDateOfFirstPasswordChange = LocalDate.now();
        }
        if (Objects.isNull(lastReactionDateToFirstPasswordChange)) {
            lastReactionDateToFirstPasswordChange = LocalDate.now();
        }
    }
}
