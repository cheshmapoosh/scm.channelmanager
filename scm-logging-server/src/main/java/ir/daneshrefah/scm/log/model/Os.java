package ir.daneshrefah.scm.log.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Os{
    private String codename;
    private String kernel;
    private String family;
    private String version;
    private String type;
    private String name;
    private String platform;
}