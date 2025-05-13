package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AbstractModel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Channel extends AbstractModel<Short> {
    @NotNull
    private Boolean active;
    private AuthenticationMethod authenticationMethod;
    private Boolean published;
    private Integer maxActivityTimeout;
    private Integer maxInactivityTimeout;
    @NotNull
    private Long maxWithdrawalPerDay;
    @NotNull
    private Long maxPersWithdrawalPerDay;
    @NotNull
    @Size(max = 200)
    private String name;
    @NotNull
    @Size(max = 3)
    private String code;
    private Boolean accessControl;
    @NotNull
    @Size(max = 3)
    private String abbreviation;
    @NotNull
    private Boolean adminPublished = false;
    private Boolean customerPublished;
    @NotNull
    private Boolean supportTwoFactorIdentification = false;
    private Integer minAge;
    private Boolean legalAvailable;
    private Boolean foreignAvailable;
    @NotNull
    private Long maxWithdrawalPerMonth;
    @NotNull
    private Long maxPersWithdrawalPerMonth;
    @NotNull
    private Boolean clientVersionCheck;
    @NotNull
    private Boolean registerCheck;
}