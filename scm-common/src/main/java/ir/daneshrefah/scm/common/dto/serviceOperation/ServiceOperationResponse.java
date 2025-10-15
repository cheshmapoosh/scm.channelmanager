package ir.daneshrefah.scm.common.dto.serviceOperation;

import ir.daneshrefah.scm.common.model.definition.Definition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceOperationResponse {
    private String id;
    private Boolean active;
    private String operationName;
    private Definition definition;
}
