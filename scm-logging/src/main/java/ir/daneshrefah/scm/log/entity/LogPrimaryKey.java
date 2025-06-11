package ir.daneshrefah.scm.log.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
