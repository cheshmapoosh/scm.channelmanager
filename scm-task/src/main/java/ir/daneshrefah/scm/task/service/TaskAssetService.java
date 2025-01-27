package ir.daneshrefah.scm.task.service;

import java.util.Optional;

public interface TaskAssetService {

    Optional<String> findCustomerNo(Long userId);
}
