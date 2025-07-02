package ir.daneshrefah.scm.log.service;

import ir.daneshrefah.scm.common.data.entity.logging.LogTraceEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConverterService {
    List<LogTraceEntity> mapToLogTraceEntity(String msg) throws Exception;
}
