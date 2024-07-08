package ir.daneshrefah.scm.process.service.util.processVariable;

import java.util.Map;

public interface ProcessVariableService {
    Map<String, Object> getProcessVariables(String processInstanceId);
     void setProcessVariables(String processInstanceId ,String variableName,Object variableValue);
     void setProcessVariables(String processInstanceId ,Map<String,Object> variable);
}