package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity(name = "CM_CHANNEL")
@Table(name = "CHANNEL", schema = "REF")
public class ChannelEntity extends AbstractEntity<Short> {
    @Id
    @SequenceGenerator(name = "CHANNEL_id_gen", sequenceName = "SQCONSTANTS", allocationSize = 1)
    @Column(name = "CHANNEL_ID", nullable = false)
    private Short id;

    @NotNull
    @Column(name = "ACTIVE", nullable = false)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "AUTHENTICATION_METHOD_ID")
    private AuthenticationMethodEntity authenticationMethod;

    @ColumnDefault("1")
    @Column(name = "PUBLISHED")
    private Boolean published;

    @ColumnDefault("180000")
    @Column(name = "MAX_ACTIVITY_TIMEOUT")
    private Integer maxActivityTimeout;

    @ColumnDefault("180000")
    @Column(name = "MAX_INACTIVITY_TIMEOUT")
    private Integer maxInactivityTimeout;

    @NotNull
    @Column(name = "MAX_WITHDRAWAL_PER_DAY", nullable = false)
    private Long maxWithdrawalPerDay;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "MAX_PERS_WITHDRAWAL_PER_DAY", nullable = false)
    private Long maxPersWithdrawalPerDay;

    @Size(max = 200)
    @NotNull
    @Convert(disableConversion = true)
    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @Size(max = 3)
    @NotNull
    @ColumnDefault("''")
    @Convert(disableConversion = true)
    @Column(name = "CODE", nullable = false, length = 3)
    private String code;

    @ColumnDefault("1")
    @Column(name = "ACCESS_CONTROL")
    private Boolean accessControl;

    @Size(max = 3)
    @NotNull
    @ColumnDefault("'A'")
    @Convert(disableConversion = true)
    @Column(name = "ABBREVIATION", nullable = false, length = 3)
    private String abbreviation;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "ADMIN_PUBLISHED", nullable = false)
    private Boolean adminPublished = false;

    @ColumnDefault("0")
    @Column(name = "CUSTOMER_PUBLISHED")
    private Boolean customerPublished;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "SPRT_TWO_FACTOR_IDENTIFICATION", nullable = false)
    private Boolean supportTwoFactorIdentification = false;

    @ColumnDefault("12")
    @Column(name = "MIN_AGE")
    private Integer minAge;

    @ColumnDefault("1")
    @Column(name = "LEGAL_AVAILABLE")
    private Boolean legalAvailable;

    @ColumnDefault("0")
    @Column(name = "FOREIGN_AVAILABLE")
    private Boolean foreignAvailable;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "MAX_WITHDRAWAL_PER_MONTH", nullable = false)
    private Long maxWithdrawalPerMonth;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "MAX_PERS_WITHDRAWAL_PER_MONTH", nullable = false)
    private Long maxPersWithdrawalPerMonth;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "CLIENT_VERSION_CHECK", nullable = false)
    private Boolean clientVersionCheck;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "REGISTER_CHECK", nullable = false)
    private Boolean registerCheck;

}