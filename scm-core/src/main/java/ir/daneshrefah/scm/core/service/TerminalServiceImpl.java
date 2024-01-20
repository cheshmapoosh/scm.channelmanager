package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.TerminalMapper;
import ir.daneshrefah.scm.core.mapper.TerminalServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.core.repository.TerminalRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceAccessRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

@RequiredArgsConstructor
@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private final ServiceRepository serviceRepository;
    private final TerminalServiceAccessRepository terminalServiceAccessRepository;

    public List<Terminal> findAllTerminals() {
        return TerminalMapper.INSTANCE.entitiesToModels(terminalRepository.findAll());
    }

    public List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId) {
        List<TerminalServiceAccessEntity> entityList = terminalServiceAccessRepository.findAllByTerminalId(terminalId);
        return TerminalServiceAccessMapper.INSTANCE.entitiesToModels(entityList);
    }

    @Override
    public TerminalServiceAccess assignServiceToTerminal(String terminalId, String serviceId) {
        if (StringUtils.isEmpty(terminalId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_TERMINAL_ID_IS_EMPTY,
                    "terminal id is empty.");
        }
        if (StringUtils.isEmpty(serviceId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_ID_IS_EMPTY,
                    "service id is empty.");
        }
        Optional<TerminalEntity> terminalEntity = terminalRepository.findById(terminalId);
        if (terminalEntity.isEmpty()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_TERMINAL_ID_IS_INVALID,
                    "terminal id is invalid.");
        }
        Optional<ServiceEntity> serviceEntity = serviceRepository.findById(serviceId);
        if (serviceEntity.isEmpty()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_ID_IS_INVALID,
                    "service id is invalid.");
        }
        TerminalServiceAccessEntity entity = new TerminalServiceAccessEntity();
        entity.setTerminal(terminalEntity.get());
        entity.setService(serviceEntity.get());
        return TerminalServiceAccessMapper.INSTANCE.toModel(terminalServiceAccessRepository.save(entity));
    }

}
