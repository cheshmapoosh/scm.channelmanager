package ir.daneshrefah.scm.provider.shetab.iso.util;

public enum ProcessCode {

    PURCHASE("000000"),
    DEBIT("010000"),
    COMMISSION("110000"),
    CARDLESS_WITHDRAWAL("120000"),
    CARDLESS_DEPOSIT("160000"),
    PINPAD_BILL_PAYMENT("170000"),
    RECONCILIATION("180000"),
    KIOSK_PURCHASE_INTERBANK_CARDS_1("186000"),
    KIOSK_PURCHASE_INTERBANK_CARDS_2("187000"),
    KIOSK_PURCHASE_LOCAL_CARDS("188000"),
    CREDIT("230000"),
    AUTHENTICATION("300000"),
    BALANCE("310000"),
    FULL_STATEMENT("320000"),
    AUTHORIZATION_ADVICE("330000"),
    MINI_STATEMENT("380000"),
    FUND_TRANSFER("400000"),
    SHETAB_POS_FUND_TRANSFER("410000"),
    SHETAB_ATM_FUND_TRANSFER("420000"),
    FUND_TRANSFER_FROM_ACCOUNT("460000"),
    //    FUND_TRANSFER_TO_ACCOUNT("470000"),
    INSURANCE_BILL_PAYMENT("480000"),
    ATM_SERVICE_BILL_PAYMENT("490000"),
    POS_SERVICE_BILL_PAYMENT("500000"),
    FUND_TRANSFER_TO_ACCOUNT("550000"),
    PIN_CHARGE("520000"),
    TOPUP("540000"),
    //    PIN2_CHANGE("870000"),
    UNKNOWN("111111"),

    //Pooya Codes
    NAB_TO_NAB_TRANSFER("408080"),
    HPS_TO_NAB_TRANSFER("406080"),
    NAB_TO_HPS_TRANSFER("408060"),
    PIN_PAD_CREDIT("417060"),
    PIN_PAD_SIMULATOR_CREDIT("417000"),
    SHETAB_TO_NAB_TRANSFER("417080"),
    NAB_TO_HPS_TRANSFER_2("418060"),
    NAB_TO_SHETAB_TRANSFER("418070"),
    POS_SIMULATOR_CREDIT("427000"),
    POS_CREDIT("427060"),
    SHETAB_TO_NAB_TRANSFER_MIRROR("427080"),
    HPS_TO_NAB_TRANSFER_MIRROR("426080"),
    NAB_TO_HPS_TRANSFER_3("428060"),
    NAB_TO_SHETAB_TRANSFER_MIRROR("428070"),
    DYNAMIC_PASSWORD_INQUIRY("300000"),
    CARD_PASSWORD_NOTIFICATION("320000"),
    CUSTOMER_CARD_LIST_INQUIRY("920000"),
    ENCRYPTION_SKD_SERVICE("930000"),
    UPDATE_PHONE_NUMBER("920000"),
    OTP_REQUEST("320000"),
    CARD_FIRST_PASSWORD_MODIFICATION("910000"),
    CARD_OTP_SERVICE("920000");

    private String code;

    ProcessCode() {
        this.code = "111111";
    }

    ProcessCode(String code) {
        this.code = code;
    }

    public static ProcessCode getValue(String code) {
        for(ProcessCode processCode : ProcessCode.values()) {
            if(code.equals(processCode.code)) {
                return processCode;
            }
        }
        return UNKNOWN;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isCredit(String code) {
        return !code.isEmpty() && (
                PIN_PAD_CREDIT.getCode().equals(code) ||
                        PIN_PAD_SIMULATOR_CREDIT.getCode().equals(code) ||
                        POS_CREDIT.getCode().equals(code) ||
                        POS_SIMULATOR_CREDIT.getCode().equals(code) ||
                        CARDLESS_DEPOSIT.getCode().equals(code));
    }

    public static boolean isCardless(String processCode) {
        return !processCode.isEmpty() && (
                CARDLESS_WITHDRAWAL.getCode().equals(processCode) ||
                        CARDLESS_DEPOSIT.getCode().equals(processCode));
    }

}
