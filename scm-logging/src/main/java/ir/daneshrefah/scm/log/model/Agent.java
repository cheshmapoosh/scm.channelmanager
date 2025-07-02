package ir.daneshrefah.scm.log.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Agent {
    private String id;
    private String type;
    private String ephemeral_id;
    private String name;
    private String version;
}
