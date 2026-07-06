package ir.daneshrefah.scm.common.service.task;

import java.util.Optional;

public interface TaskAssetService {

    Optional<String> findCustomerNo(Integer userId);
}
