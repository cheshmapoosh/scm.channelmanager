package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Data;

@Data
public class TerminalServiceAccessAssignmentResponse {
    private boolean hasTerminalAccess;
    private Service service;
}
