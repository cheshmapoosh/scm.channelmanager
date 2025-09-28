package ir.daneshrefah.scm.common.log.entity.logging;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class LogPrimaryKey implements Serializable {
    @Column(name = "SPAN_ID")
    private String spanId;
    @Column(name = "TRACE_ID")
    private String traceId;
    @Column(name = "ROW_NO")
    private int rowNo;
}
