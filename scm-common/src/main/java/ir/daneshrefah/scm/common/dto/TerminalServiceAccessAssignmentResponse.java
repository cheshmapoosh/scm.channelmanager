package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.model.service.ScmService;
import lombok.Data;

@Data
public class TerminalServiceAccessAssignmentResponse {
    private boolean hasTerminalAccess;
    private ScmService service;
}
