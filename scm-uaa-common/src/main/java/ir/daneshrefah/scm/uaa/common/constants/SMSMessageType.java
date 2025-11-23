package ir.daneshrefah.scm.uaa.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SMSMessageType {
    MB_ACTIVATION("sendFinancialReceiptSMS"),
    MB_LOGIN_BLOCKED("sendFinancialReceiptSMS"),
    MB_REGISTER_BLOCKED("sendFinancialReceiptSMS");

    private final String message;
}
