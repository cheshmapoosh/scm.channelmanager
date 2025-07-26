package ir.daneshrefah.scm.log.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogMessage {
    private Input input;
    private SpanModel payload;
    private Log log;
    private Host host;
    private String timestamp;
    private String type;
    private String loglevel;
    private String message;
    private Agent agent;
    private Ecs ecs;
    private List<String> tags = new ArrayList<>();
    private String version;
    @JsonProperty("@timestamp")
    private String atTimestamp;
    @JsonProperty("@version")
    private String atVersion;
}