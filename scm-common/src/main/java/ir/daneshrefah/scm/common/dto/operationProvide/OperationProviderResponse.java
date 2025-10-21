package ir.daneshrefah.scm.common.dto.operationProvide;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationProviderResponse{
    private String id;
    private String title;
    private String name;
    private String uri;
    private Boolean active;
    @JsonIgnore
    private String version;
}