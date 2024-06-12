package ir.daneshrefah.scm.process.service.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.process.model.filter.ProcessFilter;
import ir.daneshrefah.scm.process.model.request.HistoryRequest;
import ir.daneshrefah.scm.process.model.response.ProcessHistoryResponse;
import ir.daneshrefah.scm.process.model.response.TaskHistoryResponse;

import java.util.List;

public interface HistoryManagement {

    List<TaskHistoryResponse> findTaskHistories(HistoryRequest historyRequest) throws JsonProcessingException;

    List<ProcessHistoryResponse> findProcessHistories(ProcessFilter processFilter) throws JsonProcessingException;
}
