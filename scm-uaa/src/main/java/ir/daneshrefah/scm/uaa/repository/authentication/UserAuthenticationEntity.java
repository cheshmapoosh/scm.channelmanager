package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.common.data.entity.gateway.AuthenticationMethodEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;

import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "USER_CHANNEL_AUTHENTICATION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "USER_AUTHENTICATION_TYPE",
        discriminatorType = DiscriminatorType.INTEGER
)
@DiscriminatorValue("0")
public class UserAuthenticationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_CHANNEL_AUTHENTICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private UserEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODIFIED_BY")
    private UserEntity modifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHENTICATION_METHOD_ID", nullable = false)
    private AuthenticationMethodEntity authenticationMethod;

    @Column(name = "CREATION_DATE", nullable = false)
    private Timestamp creationDate;

    @Column(name = "MODIFICATION_DATE")
    private Timestamp modificationDate;

    @Column(name = "PASSWORD_SET_PRINTED")
    private Boolean passwordSetAndPrinted;

    @Column(name = "PRINT_COUNT")
    private Long printCount;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECTIVE_DATE")
    private Date effectiveDate;

    @Column(name = "NICK_NAME", nullable = false, length = 20)
    private String nickName;

    @Column(name = "ARCHIVE_NO", nullable = false, updatable = false)
    private Long archiveNo;

    @Column(name = "REASON", length = 1000)
    private String reason;

    @Column(name = "USER_REASON", length = 50)
    private String reasonUser;

    @Column(name = "DE_ACTIVE_REASON", length = 3)
    private String deActiveReason;

    @Column(name = "ABORT_PASS")
    private Boolean abortPass;
}