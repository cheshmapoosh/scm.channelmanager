package ir.daneshrefah.scm.log.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties
public class SpanModel{
    private Map<String,String> attributes;
    private String kind;
    private Long startEpochNanos;
    private Long endEpochNanos;
    private String traceId;
    private String spanId;
    private Map<String,String> status = new HashMap<>();
    private String parentSpanId;
    private Long startTime;
    private Long endTime;
    private List<EventData> events;
    private String name;
}
