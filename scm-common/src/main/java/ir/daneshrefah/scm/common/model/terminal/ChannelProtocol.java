package ir.daneshrefah.scm.common.model.terminal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-02
 */
public enum ChannelProtocol {

    SPRING_REST("rest"),
    DYNAMIC_REST("rest"),
    SOAP("soap"),
    JMS("jms"),
    CUSTOM("custom");

    ChannelProtocol(String code) {
        this.code = code;
    }

    private String code;

    public String getCode() {
        return code;
    }

    public static ChannelProtocol findByCode(String code) {
        for (ChannelProtocol enumValue : ChannelProtocol.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
