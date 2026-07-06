package ir.daneshrefah.scm.provider.task.service;

import java.util.Optional;

public interface TaskAssetService {

    Optional<String> findCustomerNo(Integer userId);
}
