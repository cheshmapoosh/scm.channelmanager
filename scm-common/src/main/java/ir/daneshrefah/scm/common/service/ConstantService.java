package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.model.constant.Constant;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
public interface ConstantService {

    Optional<Constant> findConstantByKey(String key);
    Optional<String> findConstantValueByKey(String key);

}
