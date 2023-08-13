package ir.daneshrefah.scm.common.model.message;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public enum EventType {

    WHOLE("whl"), TRANSFORM("trf"), SERVICE_CALL("scc");

    EventType(String code) {
        this.code = code;
    }

    private String code;

    public String getCode() {
        return code;
    }

    public static EventType findByCode(String code) {
        for (EventType enumValue : EventType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
