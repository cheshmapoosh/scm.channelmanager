package ir.daneshrefah.scm.common.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceAccessFindRequest extends ServiceFindRequest {
    private String terminalId;
    private Boolean hasTerminalAccess;
}
