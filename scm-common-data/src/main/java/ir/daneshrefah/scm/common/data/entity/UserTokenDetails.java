package ir.daneshrefah.scm.common.data.entity;

import ir.daneshrefah.scm.common.data.converter.AuthenticationMethodConverter;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "USER_TOKEN_DETAILS")
public class UserTokenDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_TOKEN_DETAILS_ID")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANNEL_ID", referencedColumnName = "LEGACY_TERMINAL_ID")
    private TerminalEntity terminal;
    @Column(name = "ACTIVE")
    private boolean active;
    @Column(name = "AUTHENTICATION_METHOD_ID")
    @Convert(converter = AuthenticationMethodConverter.class)
    private AuthenticationMethod authenticationMethod;
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private GeneralPersonEntity person;
    @Column(name = "TOKEN_TYPE")
    private String tokenType;
    @Column(name = "FROM_DATE")
    private Date accessFromDate;
    @Column(name = "TO_DATE")
    private Date accessToDate;
    @Column(name = "SERIAL_NO")
    private String otpSerialNo;
    @Column(name = "ACTIVATION_CODE")
    private String activationCode;
    @Column(name = "CREATED_BY")
    private Long createdBy;
    @Column(name = "MODIFIED_BY")
    private Integer modifiedBy;
    @Column(name = "CREATION_DATE")
    private Date creationDate;
    @Column(name = "MODIFICATION_DATE")
    private Date modificationDate;
}
