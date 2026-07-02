package ir.daneshrefah.scm.common.data.model;

import java.io.Serializable;

public enum CmAccountType implements Serializable {
    /** The SDA. */
    SDA("01"),

    /** The CCA. */
    CCA("02"),

    /** The ILA. */
    ILA("03"),

    /** The CLA. */
    CLA("04"),

    /** The LOC. */
    LOC("05"),

    GOM("06"),

    /** The UNKNOWN. */
    UNKNOWN("99");

    String code;

    CmAccountType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    /*
    DDA Demand Deposit Account. An account paying funds on demand without notice of intended withdrawal. Also known as a Checking Account. This is a deposit type account. N
    SDA Savings Deposit Account. An interest-bearing deposit account without a stated maturity, as opposed to a time deposit. This is a deposit type account. N
    CCA Credit Card Account. An account linked to a pre-approved Line of Credit where a person with satisfactory credit rating makes retail purchases or obtains cash advances using a payment card. This is a credit type account. N
    EQU Home Equity Loan. A Loan and/or line of credit that uses a home as collateral. N
    ILA Installment Loan Account. A loan repaid with interest owed, in equal periodic payments of principal and interest. This is a loan type account. N
    CLA Commercial Loan Account. A loan to a corporation, commercial enterprise, or joint venture, as opposed to a loan to a consumer. This is a loan type account. N
    CDA Certificate of Deposit. A large denomination time deposit bearing a stipulated interest, payable at maturity. This is a deposit type account. N
    LOC Consumer Line of Credit. A commitment by a Financial Institution to lend funds to an individual up to a specified amount over a specified future period. This is a credit type account. N
    MLA Mortgage Loan Account. An extension of real estate credit, usually on a long-term basis. The real estate is the lender?s security. This is a loan type account. N
    MMA Money Market Account. An interest bearing demand deposit account investing in private and government obligations with a maturity of one year or less. This is a deposit type account. N
    CMA Cash Management Account. A personal financial management account usually combining a checking account, a money market account, a brokerage margin account and a debit card. Considered to be a deposit-type account. N
    DesignatedOther A Designated ?MediaSize.Other? Account. The account designated by the financial institution as the customer?s ?other? account choice. This account choice could be provided in addition to the default account choices of checking and savings. N
    Default   N
    UNKNOWN N
    */

    public static CmAccountType findByCode(String code) {
        CmAccountType[] attrs = CmAccountType.values();
        for (CmAccountType attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return CmAccountType.UNKNOWN;
    }

}