package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.model.BaseModel;
import ir.daneshrefah.scm.common.model.common.Operator;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class TerminalAccessVoter extends BaseModel {

    private Terminal terminal;
    private Operator ageOperator;
    private Integer ageValue;
    private Operator nationalityOperator;
    private String nationalityValue;
    private Operator loyaltyOperator;
    private String loyaltyValue;
    private Operator customerTypeOperator;
    private String customerTypeValue;

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
}
