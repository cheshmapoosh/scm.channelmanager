package ir.daneshrefah.scm.common.data.model;

public enum NabAccountType {
    SAVING_ACCOUNT("10"),
    CURRENT_ACCOUNT("20"),
    MONEY_LOANED_ACCOUNT("30"),
    LONG_TERM_DEPOSIT_ACCOUNT("40"),
    GOM_BONDS("60"),
    UNKNOWN_ACCOUNT("99");

    private String code;

    NabAccountType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static NabAccountType findByCode(String code) {
        NabAccountType[] attrs = NabAccountType.values();
        for (NabAccountType attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }

        return NabAccountType.UNKNOWN_ACCOUNT;
    }
}
