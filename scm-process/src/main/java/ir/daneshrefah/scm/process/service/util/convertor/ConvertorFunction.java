package ir.daneshrefah.scm.process.service.util.convertor;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConvertorFunction {
    private static final Map<String, FunctionDelegator> map = new HashMap<>();

    @Autowired
    public ConvertorFunction(ApplicationContext context) {
        map.put("customerNo_to_person", context.getBean(ToPersonFunctionDelegator.class));
    }

    public JsonNode call(String convertor, JsonNode extraction) {
        FunctionDelegator<JsonNode, JsonNode> function = map.get(convertor);
        if (function == null) {
            throw new RuntimeException("The inserted function name does not exist");//TODO change the exception
        }
        function.init(extraction);
        return function.apply(extraction);
    }
}
