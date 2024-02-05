package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.data.mapper.TerminalMapper;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.service.TerminalInfoRequest;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntityFactory;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.TerminalServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.TerminalServiceAccessRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

@RequiredArgsConstructor
@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private final ServiceService serviceService;
    private final TerminalServiceAccessRepository terminalServiceAccessRepository;
    private final EntityManager entityManager;
    private List<Terminal> terminals;
    private List<TerminalServiceAccess> terminalServiceAccesses;

    public List<TerminalServiceAccess> findAllTerminalServiceAccesses() {
        if (null == terminalServiceAccesses) {
            terminalServiceAccesses = TerminalServiceAccessMapper.INSTANCE.entitiesToModels(terminalServiceAccessRepository.findAll());
        }
        return terminalServiceAccesses;
    }

    public List<Terminal> findAllTerminals() {
        if (null == terminals) {
            terminals = TerminalMapper.INSTANCE.entitiesToModels(terminalRepository.findAll());
        }
        return terminals;
    }

    @Override
    public Optional<Terminal> findTerminalByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return Optional.empty();
        }
        return findAllTerminals().stream().filter(terminal -> code.equals(terminal.getCode())).findFirst();
    }

    public PagedResponseData<Terminal> findAllTerminals(TerminalInfoRequest request) {
        List<Terminal> terminalList = findAllTerminals().stream()
                .filter(terminal -> null == request || null == request.getCode() || request.getCode().equals(terminal.getCode()))
                .filter(terminal -> null == request || null == request.getStatus() || request.getStatus().equals(terminal.getStatus()))
                .filter(terminal -> null == request || null == request.getSupportCheckAuthentication() || request.getSupportCheckAuthentication().equals(terminal.isSupportCheckAuthentication()))
                .filter(terminal -> null == request || null == request.getSupportCheckSecondAuthentication() || request.getSupportCheckSecondAuthentication().equals(terminal.isSupportCheckSecondAuthentication()))
                .filter(terminal -> null == request || null == request.getSupportCheckServiceAccess() || request.getSupportCheckServiceAccess().equals(terminal.isSupportCheckServiceAccess()))
                .filter(terminal -> null == request || null == request.getSupportCheckAssetAccess() || request.getSupportCheckAssetAccess().equals(terminal.isSupportCheckAssetAccess()))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, terminalList);
    }

    public List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId) {
        return findAllTerminalServiceAccesses().stream().filter(serviceAccess ->
                        terminalId.equals(serviceAccess.getTerminal().getId())
                )
                .collect(Collectors.toList());
//        List<TerminalServiceAccessEntity> entityList = terminalServiceAccessRepository.findAllByTerminalId(terminalId);
//        return TerminalServiceAccessMapper.INSTANCE.entitiesToModels(entityList);
    }

    @Override
    public Optional<TerminalServiceAccess> findTerminalServiceAccessByTerminalCodeAndServiceCode(String terminalCode, String serviceCode) {
        return findAllTerminalServiceAccesses().stream().filter(serviceAccess ->
                        terminalCode.equals(serviceAccess.getTerminal().getCode()) &&
                                serviceCode.equals(serviceAccess.getService().getCode())
                )
                .findFirst();
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
        ir.daneshrefah.scm.common.model.service.Service service = serviceService.findServiceById(serviceId);
        if (null == service) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_ID_IS_INVALID,
                    "service id is invalid.");
        }
        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setId(terminalId);
        ServiceEntity serviceEntity = ServiceEntityFactory.createEmptyServiceEntity(serviceId, service.getImplementationType());
        TerminalServiceAccessEntity entity = new TerminalServiceAccessEntity();
        entity.setTerminal(terminalEntity);
        entity.setService(serviceEntity);
        return TerminalServiceAccessMapper.INSTANCE.toModel(terminalServiceAccessRepository.save(entity));
    }

}
