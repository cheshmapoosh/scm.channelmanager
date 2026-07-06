package ir.daneshrefah.scm.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstanceRequest {
    private JsonNode transactionData;
    private JsonNode attribute;
}