package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AbstractModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CmChannel extends AbstractModel<Integer> {

    private Integer parentId;
    private Boolean active;
    private AuthenticationMethod authenticationMethod;
    private Boolean published;
    private Integer maxActivityTimeout;
    private Integer maxInactivityTimeout;
    private Long maxWithdrawalPerDay;
    private Long maxPersWithdrawalPerDay;
    private String name;
    private String code;
    private Boolean accessControl;
    private String abbreviation;
    private Boolean adminPublished = false;
    private Boolean customerPublished;
    private Boolean supportTwoFactorIdentification = false;
    private Integer minAge;
    private Boolean legalAvailable;
    private Boolean foreignAvailable;
}
