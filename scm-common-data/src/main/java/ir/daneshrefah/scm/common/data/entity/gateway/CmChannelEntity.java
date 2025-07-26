package ir.daneshrefah.scm.common.data.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CHANNEL", schema = "REF")
public class CmChannelEntity extends AbstractEntity<Integer> {
    @Id
    @SequenceGenerator(name = "CHANNEL_ID_GEN", sequenceName = "SQCONSTANTS", allocationSize = 1)
    @Column(name = "CHANNEL_ID", nullable = false)
    private Integer id;
    @Column(name = "PARENT_ID")
    private Integer parentId;
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
    @Column(name = "MAX_WITHDRAWAL_PER_DAY", nullable = false)
    private Long maxWithdrawalPerDay;
    @Column(name = "MAX_PERS_WITHDRAWAL_PER_DAY", nullable = false)
    private Long maxPersWithdrawalPerDay;
    @Column(name = "NAME", nullable = false, length = 200)
    private String name;
    @Column(name = "CODE", nullable = false, length = 3)
    private String code;
    @Column(name = "ACCESS_CONTROL")
    private Boolean accessControl;
    @Column(name = "ABBREVIATION", nullable = false, length = 3)
    private String abbreviation;
    @Column(name = "ADMIN_PUBLISHED", nullable = false)
    private Boolean adminPublished = false;
    @Column(name = "CUSTOMER_PUBLISHED")
    private Boolean customerPublished;
    @Column(name = "SPRT_TWO_FACTOR_IDENTIFICATION", nullable = false)
    private Boolean supportTwoFactorIdentification = false;
    @Column(name = "MIN_AGE")
    private Integer minAge;
    @Column(name = "LEGAL_AVAILABLE")
    private Boolean legalAvailable;
    @Column(name = "FOREIGN_AVAILABLE")
    private Boolean foreignAvailable;

}