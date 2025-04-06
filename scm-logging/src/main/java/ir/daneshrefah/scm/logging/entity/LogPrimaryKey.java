package ir.daneshrefah.scm.logging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogPrimaryKey implements Serializable {
    @Column(name = "SPAN_ID")
    private String spanId;
    @Column(name = "TRACE_ID")
    private String traceId;
}
