package ir.daneshrefah.scm.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InterBankTraceConfig {

    @JsonProperty("request-fields")
    private Map<String, List<String>> requestFields;
}