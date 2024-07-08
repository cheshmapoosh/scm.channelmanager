package ir.daneshrefah.scm.process.service.dto.processDefinition;

import lombok.Data;

@Data
public class ProcessDefinitionResponse{
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