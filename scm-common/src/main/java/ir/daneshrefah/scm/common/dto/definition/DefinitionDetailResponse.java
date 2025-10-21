package ir.daneshrefah.scm.common.dto.definition;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefinitionDetailResponse {
    private String id;
    private JsonNode details;
}
