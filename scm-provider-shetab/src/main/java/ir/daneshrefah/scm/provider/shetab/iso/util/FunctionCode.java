package ir.daneshrefah.scm.provider.shetab.iso.util;

public enum FunctionCode {
    UNKNOWN("111111"),

    ACCOUNT_INQUIRY_DEFAULT("600"),
    ACCOUNT_INQUIRY_DEPOSIT("101"), //variz be hesab ba/bedoone shenase
    PAYMENT_WITH_ID_REFAH_CARD("202"), //variz be hesab ba/bedoone shenase
    PAYMENT_WITH_ID_SHETAB_CARD("203"), //variz be hesab ba/bedoone shenase
    PAYMENT_WITHOUT_ID_REFAH_CARD("702"), //variz be hesab ba/bedoone shenase
    PAYMENT_WITHOUT_ID_SHETAB_CARD("703"); //variz be hesab ba/bedoone shenase

    private String code;

    FunctionCode(String code) {
        this.code = code;
    }

    public static FunctionCode getValue(String code) {
        for(FunctionCode processCode : FunctionCode.values()) {
            if(code.equals(processCode.code)) {
                return processCode;
            }
        }
        return UNKNOWN;
    }

    public boolean isEqualCode(String code) {
        return !code.isEmpty() && this.code.equals(code);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
