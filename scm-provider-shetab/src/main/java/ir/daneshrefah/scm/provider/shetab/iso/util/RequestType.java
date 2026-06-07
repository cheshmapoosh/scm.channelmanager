package ir.daneshrefah.scm.provider.shetab.iso.util;

import java.io.Serializable;

public enum RequestType implements Serializable {
    PAYMENT,
    FUND_TRANSFER,
    FUND_TRANSFER_TO_ACCOUNT,
    GET_BALANCE,
    BILL_PAYMENT,
    MINI_STATEMENT,
    AUTHENTICATION,
    AUTHENTICATION_LOAN_PAYMENT,
    AUTHENTICATION_TOPUP_PAYMENT,
    AUTHENTICATION_PACKAGE_PAYMENT,
    CARD_LESS_ADD,
    CARD_LESS_CANCEL,
    MODIFY_CARD_FIRST_PASS,
    DYNAMIC_PIN;
}
