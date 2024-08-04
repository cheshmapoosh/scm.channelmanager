package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.entity.ProcessStartLog;
import ir.daneshrefah.scm.repository.ProcessStartLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessStartLogService {

    private final ProcessStartLogRepository processStartLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(ProcessStartLog processStartLog) {
        processStartLogRepository.save(processStartLog);
    }
}
