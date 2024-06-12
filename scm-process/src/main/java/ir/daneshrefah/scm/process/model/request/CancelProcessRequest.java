package ir.daneshrefah.scm.process.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelProcessRequest {
    private String processId;
    private String nationalCode; //TODO Remove this
}
