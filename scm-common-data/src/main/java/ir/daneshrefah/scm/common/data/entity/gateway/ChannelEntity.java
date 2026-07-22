package ir.daneshrefah.scm.common.data.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "CM_CHANNEL")
@Table(name = "CHANNEL", schema = "REF")
public class ChannelEntity extends AbstractEntity<Integer> {
    @Id
    @SequenceGenerator(name = "CHANNEL_id_gen", sequenceName = "SQCONSTANTS", allocationSize = 1)
    @Column(name = "CHANNEL_ID", nullable = false)
    private Integer id;

    @Column(name = "PARENT_ID")
    private Integer parentId;

    @NotNull
    @Column(name = "ACTIVE", nullable = false)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "AUTHENTICATION_METHOD_ID")
    private AuthenticationMethodEntity authenticationMethod;

    @Column(name = "PUBLISHED")
    private Boolean published;

    @Column(name = "MAX_ACTIVITY_TIMEOUT")
    private Integer maxActivityTimeout;

    @Column(name = "MAX_INACTIVITY_TIMEOUT")
    private Integer maxInactivityTimeout;

    @NotNull
    @Column(name = "MAX_WITHDRAWAL_PER_DAY", nullable = false)
    private Long maxWithdrawalPerDay;

    @NotNull
    @Column(name = "MAX_PERS_WITHDRAWAL_PER_DAY", nullable = false)
    private Long maxPersWithdrawalPerDay;

    @Size(max = 200)
    @NotNull
    @Convert(disableConversion = true)
    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @Size(max = 3)
    @NotNull
    @Convert(disableConversion = true)
    @Column(name = "CODE", nullable = false, length = 3)
    private String code;

    @Column(name = "ACCESS_CONTROL")
    private Boolean accessControl;

    @Size(max = 3)
    @NotNull
    @Convert(disableConversion = true)
    @Column(name = "ABBREVIATION", nullable = false, length = 3)
    private String abbreviation;

    @NotNull
    @Column(name = "ADMIN_PUBLISHED", nullable = false)
    private Boolean adminPublished = false;

    @Column(name = "CUSTOMER_PUBLISHED")
    private Boolean customerPublished;

    @NotNull
    @Column(name = "SPRT_TWO_FACTOR_IDENTIFICATION", nullable = false)
    private Boolean supportTwoFactorIdentification = false;

    @Column(name = "MIN_AGE")
    private Integer minAge;

    @Column(name = "LEGAL_AVAILABLE")
    private Boolean legalAvailable;

    @Column(name = "FOREIGN_AVAILABLE")
    private Boolean foreignAvailable;

}