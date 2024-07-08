package ir.daneshrefah.scm.process.service.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessRequest;
import ir.daneshrefah.scm.process.service.dto.history.HistoryTaskRequest;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessResponse;

public interface HistoryManagement {
    PagedResponseData<HistoryProcessResponse> findProcessHistories(HistoryProcessRequest historyProcessRequest) throws Exception;
    PagedResponseData findTaskHistories(HistoryTaskRequest historyTaskRequest) throws JsonProcessingException;
}
