package ir.daneshrefah.scm.common.model.notification;


import ir.daneshrefah.scm.common.model.notification.constants.NotificationConstants;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum DataKey {

    TITLE           (  "title"          , NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION),
    LOGIN_TIME      (  "loginTime"      , NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION),
    TERMINAL_CODE   (  "terminalCode"   , NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION),
    TERMINAL_TITLE  (  "terminalTitle"  , NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION);

    private final String parameterName;
    private final String templateCode;

    DataKey(String parameterName, String templateCode) {
        this.parameterName = parameterName;
        this.templateCode = templateCode;
    }

    public static List<DataKey> findByTemplateCode(String templateCode) {
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
