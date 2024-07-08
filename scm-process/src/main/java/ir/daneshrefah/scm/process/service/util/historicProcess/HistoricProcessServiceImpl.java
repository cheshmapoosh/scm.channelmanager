package ir.daneshrefah.scm.process.service.util.historicProcess;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoricProcessServiceImpl implements HistoricProcessService{

    private final HistoryService historyService;

    @Override
    public HistoricProcessInstance getProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public HistoricProcessInstance getActiveProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();
    }
}
