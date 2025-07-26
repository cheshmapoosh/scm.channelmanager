package ir.daneshrefah.scm.log.service;

import ir.daneshrefah.scm.common.data.repository.logging.LogTraceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogTraceServiceImpl implements LogService {
    private final LogTraceRepository logTraceRepository;
    private final ConverterService converterService;

    public void save(String msg) {
        try {
            logTraceRepository.saveAll(converterService.mapToLogTraceEntity(msg));
        } catch (Exception e) {
            log.error("Failed to save message: {} due to error: {}", msg, e.getMessage(), e);
        }
    }
}
