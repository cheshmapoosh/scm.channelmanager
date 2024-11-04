package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Data;

@Data
public class TerminalServiceAccessAssignmentResponse {
    private boolean hasTerminalAccess;
    private Service service;
}
