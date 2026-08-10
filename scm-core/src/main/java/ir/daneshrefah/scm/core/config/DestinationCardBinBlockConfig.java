package ir.daneshrefah.scm.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DestinationCardBinBlockConfig {

    @JsonProperty("card-number-sources")
    private List<String> cardNumberSources;
}