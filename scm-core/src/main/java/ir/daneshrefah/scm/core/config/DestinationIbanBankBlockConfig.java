package ir.daneshrefah.scm.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DestinationIbanBankBlockConfig {

    @JsonProperty("iban-sources")
    private List<String> ibanSources;
}