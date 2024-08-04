package ir.daneshrefah.scm.log.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {
    @JsonProperty("inode")
    private String inode;

    @JsonProperty("path")
    private String path;

    @JsonProperty("device_id")
    private String deviceId;
}