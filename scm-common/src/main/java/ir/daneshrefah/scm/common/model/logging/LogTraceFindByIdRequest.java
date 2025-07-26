package ir.daneshrefah.scm.common.model.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogTraceFindByIdRequest {
    private String spanId;
    private String traceId;
}
