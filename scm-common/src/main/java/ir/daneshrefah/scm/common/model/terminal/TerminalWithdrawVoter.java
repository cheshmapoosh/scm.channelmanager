package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.model.BaseModel;
import ir.daneshrefah.scm.common.model.common.DurationPeriod;
import ir.daneshrefah.scm.common.model.common.Operator;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class TerminalWithdrawVoter extends BaseModel {

    private Terminal terminal;
    private Operator ageOperator;
    private Integer ageValue;
    private Operator nationalityOperator;
    private String nationalityValue;
    private Operator loyaltyOperator;
    private String loyaltyValue;
    private Operator customerTypeOperator;
    private String customerTypeValue;
    private Operator authenticationOperator;
    private List<String> authenticationValue;
    private Channel channel;
    private TerminalServiceAccess terminalServiceAccess;
    private TerminalServiceAuthenticationAccess terminalServiceAuthenticationAccess;
    private TerminalServiceChannelAccess terminalServiceChannelAccess;
    private DurationPeriod durationPeriod;
    private Integer durationCount;
    private BigDecimal minValue;
    private BigDecimal maxValue;

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Operator getAgeOperator() {
        return ageOperator;
    }

    public void setAgeOperator(Operator ageOperator) {
        this.ageOperator = ageOperator;
    }

    public Integer getAgeValue() {
        return ageValue;
    }

    public void setAgeValue(Integer ageValue) {
        this.ageValue = ageValue;
    }

    public Operator getNationalityOperator() {
        return nationalityOperator;
    }

    public void setNationalityOperator(Operator nationalityOperator) {
        this.nationalityOperator = nationalityOperator;
    }

    public String getNationalityValue() {
        return nationalityValue;
    }

    public void setNationalityValue(String nationalityValue) {
        this.nationalityValue = nationalityValue;
    }

    public Operator getLoyaltyOperator() {
        return loyaltyOperator;
    }

    public void setLoyaltyOperator(Operator loyaltyOperator) {
        this.loyaltyOperator = loyaltyOperator;
    }

    public String getLoyaltyValue() {
        return loyaltyValue;
    }

    public void setLoyaltyValue(String loyaltyValue) {
        this.loyaltyValue = loyaltyValue;
    }

    public Operator getCustomerTypeOperator() {
        return customerTypeOperator;
    }

    public void setCustomerTypeOperator(Operator customerTypeOperator) {
        this.customerTypeOperator = customerTypeOperator;
    }

    public String getCustomerTypeValue() {
        return customerTypeValue;
    }

    public void setCustomerTypeValue(String customerTypeValue) {
        this.customerTypeValue = customerTypeValue;
    }

    public Operator getAuthenticationOperator() {
        return authenticationOperator;
    }

    public void setAuthenticationOperator(Operator authenticationOperator) {
        this.authenticationOperator = authenticationOperator;
    }

    public List<String> getAuthenticationValue() {
        return authenticationValue;
    }

    public void setAuthenticationValue(List<String> authenticationValue) {
        this.authenticationValue = authenticationValue;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public TerminalServiceAccess getTerminalServiceAccess() {
        return terminalServiceAccess;
    }

    public void setTerminalServiceAccess(TerminalServiceAccess terminalServiceAccess) {
        this.terminalServiceAccess = terminalServiceAccess;
    }

    public TerminalServiceAuthenticationAccess getTerminalServiceAuthenticationAccess() {
        return terminalServiceAuthenticationAccess;
    }

    public void setTerminalServiceAuthenticationAccess(TerminalServiceAuthenticationAccess terminalServiceAuthenticationAccess) {
        this.terminalServiceAuthenticationAccess = terminalServiceAuthenticationAccess;
    }

    public TerminalServiceChannelAccess getTerminalServiceChannelAccess() {
        return terminalServiceChannelAccess;
    }

    public void setTerminalServiceChannelAccess(TerminalServiceChannelAccess terminalServiceChannelAccess) {
        this.terminalServiceChannelAccess = terminalServiceChannelAccess;
    }

    public DurationPeriod getDurationPeriod() {
        return durationPeriod;
    }

    public void setDurationPeriod(DurationPeriod durationPeriod) {
        this.durationPeriod = durationPeriod;
    }

    public Integer getDurationCount() {
        return durationCount;
    }

    public void setDurationCount(Integer durationCount) {
        this.durationCount = durationCount;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }
}
