package ir.daneshrefah.scm.process.model.constant;

import java.util.Arrays;

public enum ProcessState {

    ACTIVE("active", "فعال"),//running process instance
    SUSPENDED("suspended", "معلق"),//suspended process instances
    COMPLETED("completed", "کامل شده"),//completed through normal end event
    EXTERNALLY_TERMINATED("externally_terminated", "خاتمه یافته خارجی"),//terminated externally, for instance through REST API
    INTERNALLY_TERMINATED("internally_terminated", "خاتمه یافته داخلی"),//terminated internally, for instance by terminating boundary event
    CANCELED("canceled", "لغو شده");//canceled process instances

    private final String stateCode;
    private final String stateDescription;

    ProcessState(String stateCode, String stateDescription) {
        this.stateCode = stateCode;
        this.stateDescription = stateDescription;
    }

    public static String getStatusDescription(String stateCode) {
        return Arrays.stream(ProcessState.values())
                .filter(state -> state.name().equals(stateCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status code: " + stateCode)) //TODO change this exception with custom
                .stateDescription;
    }
}
