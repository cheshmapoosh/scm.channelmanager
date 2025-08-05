package ir.daneshrefah.scm.log.service;

import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.log.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class LogTraceConverterService implements ConverterService {

    private final LogService logService;
    private final SpanLogConverterService spanLogConverterService;

    @Override
    public void convertAndPersist(String message) throws Exception {
        List<LogTraceEntity> entities = convert(message);
        logService.saveAll(entities);
    }

    private List<LogTraceEntity> convert(String message) throws Exception {
        return spanLogConverterService.mapToLogTraceEntity(message);
    }
}
