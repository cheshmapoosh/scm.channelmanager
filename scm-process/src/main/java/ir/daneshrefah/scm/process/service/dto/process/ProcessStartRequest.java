package ir.daneshrefah.scm.process.service.dto.process;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ProcessStartRequest {
    private String processKey;
    private JsonNode data;
}
