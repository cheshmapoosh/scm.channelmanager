package ir.daneshrefah.scm.process.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessInstanceResponse {
    private Long activeCount;
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
