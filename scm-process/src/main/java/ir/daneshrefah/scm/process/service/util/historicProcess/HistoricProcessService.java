package ir.daneshrefah.scm.process.service.util.historicProcess;

import org.camunda.bpm.engine.history.HistoricProcessInstance;

public interface HistoricProcessService {
     HistoricProcessInstance getProcessInstance(String processInstanceId);
     HistoricProcessInstance getActiveProcessInstance(String processInstanceId);
}
