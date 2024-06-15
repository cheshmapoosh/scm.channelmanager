package ir.daneshrefah.scm.process.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStartRequest {
    private String processKey;
    private Map<String, Object> data;
}
