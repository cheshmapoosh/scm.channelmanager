package ir.daneshrefah.scm.common.error;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class ExceptionInformation {
    private MessageStatus messageStatus;
    private boolean dynamicMessage;
    private ExceptionDynamicMessage exceptionDynamicMessage;
    private Map<String,String> parameter;

}
