package ir.daneshrefah.scm.common.transformerUtil.constant;

public enum TransactionType {
    WITHDRAWAL_DEPOSIT("0"),
    WITHDRAWAL("1"),
    DEPOSIT("2");

    private String code;

    TransactionType(String code){
        this.code =code;
    }


    public static TransactionType getByCode(String code) {

        for (TransactionType type : TransactionType.values()) {
            if (code.equals(type.getCode())) {
                return type;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
