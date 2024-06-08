package ir.daneshrefah.scm.common.error;

import ir.daneshrefah.scm.common.constant.BundleParameterPattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class ExceptionDynamicMessage {
    private String bundleKey;
    private Map<String,String> parameters = new HashMap<>();

    public ExceptionDynamicMessage addParameter(String key,String value){
        parameters.put(key,value);
        return this;
    }
}
