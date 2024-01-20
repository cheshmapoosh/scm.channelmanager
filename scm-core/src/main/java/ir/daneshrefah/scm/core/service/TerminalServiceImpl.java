package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.TerminalMapper;
import ir.daneshrefah.scm.core.mapper.TerminalServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.TerminalRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceAccessRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

@RequiredArgsConstructor
@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private final ServiceService serviceService;
    private final TerminalServiceAccessRepository terminalServiceAccessRepository;
    private final EntityManager entityManager;
    private List<Terminal> terminals;

    public List<Terminal> findAllTerminals() {
        if (null == terminals) {
            terminals = TerminalMapper.INSTANCE.entitiesToModels(terminalRepository.findAll());
        }
        return terminals;
    }

    public List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId) {
        List<TerminalServiceAccessEntity> entityList = terminalServiceAccessRepository.findAllByTerminalId(terminalId);
        return TerminalServiceAccessMapper.INSTANCE.entitiesToModels(entityList);
    }

    public boolean checkTerminalExistById(String terminalId) {
        if (StringUtils.isEmpty(terminalId)) {
            return false;
        }
        return findAllTerminals().stream().anyMatch(terminal -> terminalId.equals(terminal.getId()));
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
        if (!checkTerminalExistById(terminalId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_TERMINAL_ID_IS_INVALID,
                    "terminal id is invalid.");
        }
        if (!serviceService.checkServiceExistById(serviceId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_ID_IS_INVALID,
                    "service id is invalid.");
        }
        TerminalEntity terminalEntity = entityManager.getReference(TerminalEntity.class, terminalId);
        ServiceEntity serviceEntity = entityManager.getReference(ServiceEntity.class, serviceId);
        TerminalServiceAccessEntity entity = new TerminalServiceAccessEntity();
        entity.setTerminal(terminalEntity);
        entity.setService(serviceEntity);
        return TerminalServiceAccessMapper.INSTANCE.toModel(terminalServiceAccessRepository.save(entity));
    }

}
