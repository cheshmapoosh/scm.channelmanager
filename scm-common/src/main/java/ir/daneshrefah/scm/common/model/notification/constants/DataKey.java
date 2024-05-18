package ir.daneshrefah.scm.common.model.notification.constants;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum DataKey {

    TITLE           (  "title"          , NotificationTemplate.AUTHENTICATION),
    LOGIN_TIME      (  "loginTime"      , NotificationTemplate.AUTHENTICATION),
    TERMINAL_CODE   (  "terminalCode"   , NotificationTemplate.AUTHENTICATION),
    TERMINAL_TITLE  (  "terminalTitle"  , NotificationTemplate.AUTHENTICATION),
    OTP_CODE        (  "otp-code"       , NotificationTemplate.AUTHENTICATION );


    private final String parameterName;
    private final NotificationTemplate templateCode;

    DataKey(String parameterName, NotificationTemplate templateCode) {
        this.parameterName = parameterName;
        this.templateCode = templateCode;
    }

    public static List<DataKey> findByTemplateCode(NotificationTemplate templateCode) {
        return Arrays.stream(values())
                .filter(dataKey -> dataKey.getTemplateCode().equals(templateCode))
                .collect(Collectors.toList());
    }

    public static DataKey findByParameterName(String parameterName) throws NullPointerException{
        for (DataKey value : values()) {
            if (value.getParameterName().equals(parameterName)){
                return value;
            }
        }
        return null;
    }
}
