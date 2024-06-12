package ir.daneshrefah.scm.process.model.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProcessDefinitionResponse {
    private String id;
    private String key;
    private String category;
    private String description;
    private String name;
    private int version;
    private String deploymentId;
    private boolean suspended;
    private String versionTag;
    private Integer historyTimeToLive;
}
