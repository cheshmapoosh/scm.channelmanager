package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.data.mapper.TerminalMapper;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.service.terminal.*;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntityFactory;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.TerminalServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.TerminalServiceAccessRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TerminalServiceImpl implements TerminalService {

    private static final Map<String, Long> LEGACY_TERMINAL_CODE_ID_CACHE = new ConcurrentHashMap<>();
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
    public Optional<Terminal> findTerminalById(String id) {
        if (StringUtils.isEmpty(id)) {
            return Optional.empty();
        }
        return findAllTerminals().stream().filter(terminal -> id.equals(terminal.getId())).findFirst();
    }

    @Override
    public Optional<Terminal> findTerminalByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return Optional.empty();
        }
        return findAllTerminals().stream().filter(terminal -> code.equalsIgnoreCase(terminal.getCode())).findFirst();
    }

    public PagedResponseData<Terminal> findAllTerminals(TerminalFindRequest request) {
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
                        terminalCode.equalsIgnoreCase(serviceAccess.getTerminal().getCode()) &&
                                serviceCode.equalsIgnoreCase(serviceAccess.getService().getCode())
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
    public TerminalServiceAccess assignServiceToTerminal(TerminalServiceAssignmentRequest request) {
        String terminalId = request.getTerminalId();
        String serviceId = request.getServiceId();
        if (StringUtils.isEmpty(terminalId)) {
            throw new MissingRequiredInputException("terminalId");
        }
        if (StringUtils.isEmpty(serviceId)) {
            throw new MissingRequiredInputException("serviceId");
        }
        Optional<Terminal> terminal = findTerminalById(terminalId);
        if (terminal.isEmpty()) {
            terminal = findTerminalByCode(terminalId);
        }
        if (terminal.isEmpty()) {
            throw new InvalidInputException("terminalId");
        }
        ir.daneshrefah.scm.common.model.service.Service service = serviceService.findServiceById(serviceId);
        if (null == service) {
            service = serviceService.findServiceByCode(serviceId);
        }
        if (null == service) {
            throw new InvalidInputException("serviceId");
        }
        TerminalEntity terminalEntity = new TerminalEntity();
        terminalEntity.setId(terminal.get().getId());
        ServiceEntity serviceEntity = ServiceEntityFactory.createEmptyServiceEntity(service.getId(), service.getImplementationType());
        TerminalServiceAccessEntity entity = new TerminalServiceAccessEntity();
        entity.setTerminal(terminalEntity);
        entity.setService(serviceEntity);
        return TerminalServiceAccessMapper.INSTANCE.toModel(terminalServiceAccessRepository.save(entity));
    }

    @Override
    public Terminal craeteTerminal(TerminalCreateRequest request) {
        validateTerminalCreateRequest(request);
        checkTerminalCodeDuplication(request);
        Long legacyTerminalId = findLegacyTerminal(request.getCode());
        TerminalEntity entity = new TerminalEntity();
        entity.setLegacyTerminalId(legacyTerminalId);
        entity.setCode(request.getCode());
        entity.setStatus(request.getStatus());
        entity.setTitle(request.getTitle());
        entity.setSupportCheckAssetAccess(request.isSupportCheckAssetAccess());
        entity.setSupportCheckSecondAuthentication(request.isSupportCheckSecondAuthentication());
        entity.setSupportCheckAuthentication(request.isSupportCheckAuthentication());
        entity.setSupportCheckServiceAccess(request.isSupportCheckServiceAccess());
        entity.setSupportCustomerInjection(request.isSupportCustomerInjection());
        //adding base details -> it should be filled by jpa audit
        entity.setCreateDate(LocalDateTime.now());
        entity.setLastEditDate(LocalDateTime.now());
        Terminal model = TerminalMapper.INSTANCE.toModel(terminalRepository.save(entity));
        return addTerminalListCache(model);
    }

    @Override
    public void deleteTerminal(TerminalDeleteRequest request) {
        validateTerminalDeleteRequest(request);
        terminalRepository
                .findById(request.getId())
                .ifPresentOrElse(found -> {
                    //if record version passed.
                    try {
                        terminalRepository.delete(found);
                    } catch (ObjectOptimisticLockingFailureException e) {
                        throw new RecordVersionException("terminal");
                    }
                    removeTerminalListCache(TerminalMapper.INSTANCE.toModel(found));
                }, () -> {
                    throw new RecordVersionException("terminal");
                });
    }

    @Override
    public Terminal editTerminal(TerminalEditRequest request) {
        validateTerminalEditRequest(request);
        Terminal found = terminalRepository.findById(request.getId())
                .map(TerminalMapper.INSTANCE::toModel)
                .orElseThrow(() -> new NoMatchRecordFoundException(request.getCode()));
        if (found.getLastEditDate().equals(request.getLastEditDate())) {
            dynamicUpdateTerminalEntity(found, request);
            try {
                terminalRepository.save(TerminalMapper.INSTANCE.toEntity(found));
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new RecordVersionException("terminal");
            }
            removeTerminalListCache(found);
            return addTerminalListCache(found);
        } else {
            throw new RecordVersionException("terminal");
        }
    }

    private void dynamicUpdateTerminalEntity(Terminal terminal, TerminalEditRequest request) {
        terminalCodeDynamicUpdate(terminal, request);
        terminal.setTitle(Objects.nonNull(request.getTitle()) ? request.getTitle() : terminal.getTitle());
        terminal.setStatus(Objects.nonNull(request.getStatus()) ? request.getStatus() : terminal.getStatus());
        terminal.setSupportCheckAssetAccess(Objects.nonNull(request.getSupportCheckAssetAccess()) ? request.getSupportCheckAssetAccess() : terminal.isSupportCheckAssetAccess());
        terminal.setSupportCheckServiceAccess(Objects.nonNull(request.getSupportCheckServiceAccess()) ? request.getSupportCheckServiceAccess() : terminal.isSupportCheckServiceAccess());
        terminal.setSupportCheckAuthentication(Objects.nonNull(request.getSupportCheckAuthentication()) ? request.getSupportCheckAuthentication() : terminal.isSupportCheckAuthentication());
        terminal.setSupportCheckSecondAuthentication(Objects.nonNull(request.getSupportCheckSecondAuthentication()) ? request.getSupportCheckSecondAuthentication() : terminal.isSupportCheckSecondAuthentication());
        terminal.setSupportCustomerInjection(Objects.nonNull(request.getSupportCustomerInjection()) ? request.getSupportCustomerInjection() : terminal.isSupportCustomerInjection());
        terminal.setLastEditDate(LocalDateTime.now());
    }

    private void terminalCodeDynamicUpdate(Terminal terminal, TerminalEditRequest request) {
        if (Objects.nonNull(request.getCode()) && !request.getCode().isBlank() && !terminal.getCode().equals(request.getCode())) {
            request.setCode(request.getCode().toUpperCase());
            terminals
                    .stream()
                    .filter(t -> t.getCode().equals(request.getCode()))
                    .findFirst()
                    .ifPresent(t -> {
                        throw new DuplicatedRecordFoundException(t.getCode());
                    });
            terminal.setCode(request.getCode());
            terminal.setLegacyTerminalId(findLegacyTerminal(request.getCode()));
        }
    }

    private void validateTerminalEditRequest(TerminalEditRequest request) {
        String id = request.getId();
        LocalDateTime lastEditDate = request.getLastEditDate();
        //required data
        if (Objects.isNull(id) || id.isBlank()) {
            throw new InvalidInputException("id");
        } else if (Objects.isNull(lastEditDate)) {
            throw new InvalidInputException("lastEditDate");
        }
    }

    private void validateTerminalDeleteRequest(TerminalDeleteRequest request) {
        LocalDateTime lastEditDate = request.getLastEditDate();
        String id = request.getId();
        if (Objects.isNull(lastEditDate)) {
            throw new InvalidInputException("lastEditDate");
        } else if (Objects.isNull(id) || id.isBlank()) {
            throw new InvalidInputException("id");
        }
    }

    private void checkTerminalCodeDuplication(TerminalCreateRequest request) {
        String code = request.getCode();
        findAllTerminals()
                .stream()
                .filter(terminal -> terminal.getCode().equals(code))
                .findFirst().ifPresent(terminal -> {
                    throw new DuplicatedRecordFoundException(code);
                });
    }

    private Long findLegacyTerminal(String code) {
        return LEGACY_TERMINAL_CODE_ID_CACHE
                .computeIfAbsent(code, toCacheCode -> {
                    Long legacyTerminalId = terminalRepository.findLegacyTerminalId(code);
                    if (Objects.isNull(legacyTerminalId)) {
                        throw new InvalidInputException("code");
                    }
                    //if the method could not find id it means the terminal code
                    //was wrong.
                    return legacyTerminalId;
                });

    }

    private void validateTerminalCreateRequest(TerminalCreateRequest request) {
        String code = request.getCode();
        String title = request.getTitle();
        TerminalStatus status = request.getStatus();
        if (Objects.isNull(code) || code.isBlank()) {
            throw new InvalidInputException("code");
        } else if (Objects.isNull(title) || title.isBlank()) {
            throw new InvalidInputException("title");
        } else if (Objects.isNull(status)) {
            throw new InvalidInputException("status");
        }
        //normalize input
        request.setCode(request.getCode().toUpperCase());
    }

    private synchronized void removeTerminalListCache(Terminal terminal) {
        Iterator<Terminal> iterator = terminals.iterator();
        while (iterator.hasNext()) {
            Terminal cached = iterator.next();
            if (cached.getId().equals(terminal.getId())) {
                iterator.remove();
                return;
            }
        }
    }

    private synchronized Terminal addTerminalListCache(Terminal terminal) {
        //adding refresh record to cache list
        terminal = terminalRepository
                .findById(terminal.getId())
                .map(TerminalMapper.INSTANCE::toModel)
                .orElseThrow(() -> new NoMatchRecordFoundException("terminal"));
        terminals.add(terminal);
        return terminal;
    }

}
