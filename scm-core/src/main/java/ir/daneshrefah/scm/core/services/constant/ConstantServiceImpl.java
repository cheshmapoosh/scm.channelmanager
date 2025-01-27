package ir.daneshrefah.scm.core.services.constant;

import ir.daneshrefah.scm.common.model.constant.Constant;
import ir.daneshrefah.scm.common.service.ConstantService;
import ir.daneshrefah.scm.core.repository.ConstantRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@RequiredArgsConstructor
@Service
public class ConstantServiceImpl implements ConstantService {

    private final ConstantRepository constantRepository;
    private final ConstantCache constantCache;

    @Override
    public Optional<Constant> findConstantByKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> findConstantValueByKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return Optional.empty();
        }
        return Optional.empty();
    }

}
