package ir.daneshrefah.scm.common.service;

import lombok.Data;

@Data
public class ServiceAccessFindRequest extends ServiceFindRequest {
    private String terminalId;
    private Boolean hasTerminalAccess;
}
