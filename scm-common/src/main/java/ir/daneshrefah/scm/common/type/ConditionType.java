package ir.daneshrefah.scm.common.type;

public enum ConditionType {
    RATE(1), WITHDRAW(2);
    ConditionType(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static ConditionType findByCode(Integer code) {
        if (ConditionType.RATE.getCode()==code){
            return RATE;
        }
        if (ConditionType.WITHDRAW.getCode()==code){
            return WITHDRAW;
        }
        return null;
    }
}
