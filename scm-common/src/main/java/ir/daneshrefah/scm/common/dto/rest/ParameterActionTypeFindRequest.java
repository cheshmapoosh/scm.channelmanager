package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Data
public class ParameterActionTypeFindRequest implements RequestData {

    private String usage;

    public enum ActionTypeUsage {
        REQUEST, RESPONSE, ALL ,CONFIG;

        public static ActionTypeUsage fromValue(String value) {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return Arrays.stream(values())
                    .filter(actionTypeUsage -> actionTypeUsage.name().equalsIgnoreCase(value))
                    .findAny().orElse(null);
        }
    }
}
