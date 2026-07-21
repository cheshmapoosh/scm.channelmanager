package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.common.data.entity.gateway.AuthenticationMethodEntity;
import ir.daneshrefah.scm.common.data.entity.gateway.ChannelEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@DiscriminatorValue("1")
public class UserChannelAuthentication extends UserAuthenticationEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANNEL_ID")
    private ChannelEntity channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECOND_LEVEL_AUTH_METHOD_ID")
    private AuthenticationMethodEntity secondLevelAuthenticationMethod;

    @Column(name = "FIRST_PASSWORD", nullable = false, length = 128)
    private String password;

    @Column(name = "SECOND_PASSWORD", length = 128)
    private String secondPassword;

    @Column(name = "CHANNEL_ACCESS_PARAM", length = 32)
    private String channelAccessParameter;

    @Column(name = "BRANCH_CODE", length = 32)
    private String creatorBranchCode;

    @Column(name = "FROM_DATE")
    private Timestamp accessFromDate;

    @Column(name = "TO_DATE")
    private Timestamp accessToDate;

    @Column(name = "ACTIVE")
    private boolean active;

    @Column(name = "OTP_SERIAL_NO", length = 10)
    private String otpSerialNo;

    @Column(name = "PIN_BASED_PASSWORD", length = 128)
    private String pinBasedPassword;

    @Column(name = "PATTERN_BASED_PASSWORD", length = 128)
    private String patternBasedPassword;

    @Column(name = "LAST_DATE_OF_PASSWORD_CHANGE")
    private Date lastDateOfFirstPasswordChange;

    @Column(name = "LAST_REACTION_DATE_TO_PASSWORD")
    private Date lastReactionDateToFirstPasswordChange;
}