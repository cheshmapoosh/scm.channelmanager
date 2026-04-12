package ir.daneshrefah.scm.task.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ProcessCodeEnum {

    INTERNAL(1, "internal"),
    ACH(2, "ach"),
    RTGS(3, "rtgs"),
    POL(4, "pol"),
    RECURRING_ADD(5, "recurringAdd"),
    RECURRING_EDIT(6, "recurringEdit"),
    BILL_PAYMENT(7, "billPayment"),
    INSURANCE_PAYMENT(8, "insurancePayment"),
    PAYMENT_ORDER_INTERNAL(9, "paymentOrderInternal"),
    PAYMENT_ORDER_ACH(10, "paymentOrderAch"),
    PAYMENT_ORDER_RTGS(11, "paymentOrderRtgs"),
    CHEQUE_BOOK_ISSUANCE_ADD(12, "chequeBookIssuanceAdd"),
    CHEQUE_BOOK_ISSUANCE_DELETE(13, "chequeBookIssuanceDelete"),
    ACH_BATCH(14, "achBatch"),
    BILL_BATCH(15, "billBatch"),
    INTERNAL_BATCH(16, "internalBatch"),
    INSURANCE_BATCH(17, "insuranceBatch"),
    RECURRING_ADD_ACH(18, "recurringAddAch"),
    RECURRING_ADD_BATCH_ACH(19, "recurringAddBatchAch"),
    PROCUREMENT_AGENT_PERMISSION(20, "procurementAgentPermission");

    private final Integer code;
    private final String transactionTypeName;

    public static ProcessCodeEnum findByCode(Integer code) {
        return Arrays
                .stream(ProcessCodeEnum.values())
                .filter(transactionTypeEnum -> transactionTypeEnum.getCode().equals(code))
                .findFirst().orElse(null);
    }
}