package ir.daneshrefah.scm.common.model.notification.constants;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum DataKey {

    TITLE           (  "title"          , TemplateCode.AUTHENTICATION),
    LOGIN_TIME      (  "loginTime"      , TemplateCode.AUTHENTICATION),
    TERMINAL_CODE   (  "terminalCode"   , TemplateCode.AUTHENTICATION),
    TERMINAL_TITLE  (  "terminalTitle"  , TemplateCode.AUTHENTICATION),
    OTP_CODE        (  "otp-code"       ,TemplateCode.AUTHENTICATION );


    private final String parameterName;
    private final TemplateCode templateCode;

    DataKey(String parameterName, TemplateCode templateCode) {
        this.parameterName = parameterName;
        this.templateCode = templateCode;
    }

    public static List<DataKey> findByTemplateCode(TemplateCode templateCode) {
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
