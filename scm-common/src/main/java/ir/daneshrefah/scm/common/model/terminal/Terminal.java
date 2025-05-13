package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Setter
public class Terminal extends AbstractAuditableModel<String> {

    private String code;
    private String title;
    private Long legacyTerminalId;
    private TerminalStatus status;
    private boolean supportCheckAuthentication;
    private boolean supportCheckSecondAuthentication;
    private boolean supportCheckServiceAccess;
    private boolean supportCheckAssetAccess;
    private boolean supportCustomerInjection;
    private BigDecimal legacyMaxWithdrawalPerMonth;
    private BigDecimal legacyMaxWithdrawalPerDay;


}
