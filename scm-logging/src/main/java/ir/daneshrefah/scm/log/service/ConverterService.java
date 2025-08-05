package ir.daneshrefah.scm.log.service;

import org.springframework.stereotype.Service;

@Service
public interface ConverterService {
    void convertAndPersist(String message) throws Exception;
}
