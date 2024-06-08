package ir.daneshrefah.scm.common.error;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author abdolahi.d
 * @apiNote Build parametric exception message.
 */
public class ExceptionInformationBuilder {

    private final Map<String, String> parametersMap = new HashMap<>();
    ExceptionInformation exceptionInformation;
    private ExceptionInformationBuilder() {
    }

    public static ExceptionInformationBuilder createInstance() {
        ExceptionInformationBuilder exceptionInformationBuilder = new ExceptionInformationBuilder();
        exceptionInformationBuilder.exceptionInformation = new ExceptionInformation();
        return exceptionInformationBuilder;
    }


    public ExceptionInformationBuilder defineMessageParameter(String parameterName, String source) {
        parametersMap.put(parameterName, source);
        return this;
    }

    public ExceptionInformationBuilder dynamicMessage(ExceptionDynamicMessage exceptionDynamicMessage) {
        exceptionInformation.setDynamicMessage(true);
        exceptionInformation.setExceptionDynamicMessage(exceptionDynamicMessage);
        return this;
    }

    public ExceptionInformation buildWithStatus(MessageStatus messageStatus) {
        return exceptionInformation
                .setMessageStatus(messageStatus)
                .setParameter(this.parametersMap);
    }

}
