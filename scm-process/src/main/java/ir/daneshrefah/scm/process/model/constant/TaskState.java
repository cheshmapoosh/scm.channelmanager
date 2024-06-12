package ir.daneshrefah.scm.process.model.constant;

import java.util.Arrays;

public enum TaskState {
    COMPLETED("completed", "تایید"),
    DELETED("deleted", "حذف"),
    CANCELED("canceled", "انصراف"),
    EXPIRED("expired", "منقضی"),
    ERROR("error", "خطا"),
    MIGRATION("migration", "مهاجرت");

    private final String stateCode;
    private final String stateDescription;

    TaskState(String stateCode, String stateDescription) {
        this.stateCode = stateCode;
        this.stateDescription = stateDescription;
    }

    public static String getStateDescription(String stateCode) {
        if (stateCode == null) {
            return "";
        }
        return Arrays.stream(TaskState.values())
                .filter(deletionStatus -> deletionStatus.stateCode.equalsIgnoreCase(stateCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status code: " + stateCode)) //TODO change this exception with custom
                .stateDescription;
    }
}
