package ir.daneshrefah.scm.core.services;

import ir.daneshrefah.scm.common.annotation.LegacyChannelManger;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.data.mapper.TerminalMapper;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.dto.terminal.*;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.terminal.LegacyTerminal;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.entity.service.ScmServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.TerminalServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceAccessRepository;
import ir.daneshrefah.scm.core.repository.TransformerRelationRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TerminalServiceImpl extends TerminalService {

    private static final Map<String, Long> LEGACY_TERMINAL_CODE_ID_CACHE = new ConcurrentHashMap<>();
    private static final List<LegacyTerminal> LEGACY_TERMINALS = new CopyOnWriteArrayList<>();
    private final TerminalRepository terminalRepository;
    private final TerminalServiceAccessRepository terminalServiceAccessRepository;
    private final TransformerRelationRepository transformerRelationRepository;
    private final ServiceRepository serviceRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TerminalServiceAccessMapper terminalServiceAccessMapper;
    private final TerminalMapper terminalMapper;
    private List<Terminal> terminals;
    private List<TerminalServiceAccess> terminalServiceAccesses;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<TerminalServiceAccess> findAllTerminalServiceAccesses() {
        if (null == terminalServiceAccesses || terminalServiceAccesses.isEmpty()) {
            synchronized (this) {
                terminalServiceAccesses = terminalServiceAccessMapper.entitiesToModels(terminalServiceAccessRepository.findAll());
            }
        }
        return terminalServiceAccesses;
    }

    public List<Terminal> findAllTerminals() {
        if (Objects.isNull(terminals) || terminals.isEmpty()) {
            synchronized (this) {
                if (Objects.isNull(terminals) || terminals.isEmpty()) {
                    terminals = terminalMapper.entitiesToModels(terminalRepository.findAll());
                }
            }
        }
        return terminals;
    }


    @Override
    public void evictCache() {
        if (Objects.nonNull(terminals)) {
            terminals.clear();
        }
        if (Objects.nonNull(terminalServiceAccesses)) {
            terminalServiceAccesses.clear();
        }
    }

    @Override
    public Optional<Terminal> findTerminalById(String id) {
        if (StringUtils.isEmpty(id)) {
            return Optional.empty();
        }
        return terminalRepository.findById(id).map(terminalMapper::toModel);
    }

    @Override
    public Optional<Terminal> findTerminalByLegacyId(Integer id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return findAllTerminals().stream().filter(terminal -> Long.valueOf(id).equals(terminal.getLegacyTerminalId())).findFirst();
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
                .filter(terminal -> null == request || null == request.getCreator() || terminal.getCreator().equalsIgnoreCase(request.getCreator()))
                .filter(terminal -> null == request || null == request.getEditor() || terminal.getLastEditor().equalsIgnoreCase(request.getEditor()))
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
//        return terminalServiceAccessMapper.entitiesToModels(entityList);
    }

    @Override
    public Optional<TerminalServiceAccess> findTerminalServiceAccessByTerminalCodeAndServiceCode(String terminalCode, String serviceCode) {
        return findAllTerminalServiceAccesses().stream().filter(serviceAccess ->
                        terminalCode.equalsIgnoreCase(serviceAccess.getTerminal().getCode()) &&
                        serviceCode.equalsIgnoreCase(serviceAccess.getService().getCode())
                )
                .findFirst();
    }

    @Override
    public TerminalServiceAccess assignServiceToTerminal(TerminalServiceAssignmentRequest request) {
        TerminalEntity terminalEntity = terminalRepository.findById(request.getTerminalId()).orElseThrow(() -> new InvalidInputException("terminalId"));
        ScmServiceEntity serviceEntity = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new InvalidInputException("serviceId"));
        terminalServiceAccessRepository
                .findByTerminal_IdAndService_Id(terminalEntity.getId(), serviceEntity.getId())
                .ifPresentOrElse(found -> {
                    throw new DuplicatedRecordFoundException("terminalServiceAccess");
                }, () -> {
                    TerminalServiceAccessEntity entity = new TerminalServiceAccessEntity();
                    entity.setTerminal(terminalEntity);
                    entity.setService(serviceEntity);
                    terminalServiceAccessRepository.save(entity);
                });
        evictCache();
        return findAllTerminalServiceAccesses()
                .stream()
                .filter(serviceAccess -> serviceAccess.getTerminal().getId().equals(request.getTerminalId()))
                .filter(serviceAccess -> serviceAccess.getService().getId().equals(request.getServiceId()))
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException("terminalServiceAccess"));
    }

    @Override
    public void revokeServiceFromTerminal(TerminalServiceAssignmentRequest request) {
        TerminalEntity terminalEntity = terminalRepository.findById(request.getTerminalId()).orElseThrow(() -> new InvalidInputException("terminalId"));
        ScmServiceEntity serviceEntity = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new InvalidInputException("serviceId"));
        terminalServiceAccessRepository
                .findByTerminal_IdAndService_Id(terminalEntity.getId(), serviceEntity.getId())
                .ifPresentOrElse(found -> {
                    terminalServiceAccessRepository.delete(found);
                    evictCache();
                }, () -> {
                    throw new NoMatchRecordFoundException("terminalServiceAccess");
                });
    }

    @Override
    public List<Terminal> findAllTerminalAccessOnService(String serviceId) {
        ValidationUtils.checkBlankString(serviceId, () -> new MissingRequiredInputException("serviceId"));
        ScmServiceEntity serviceEntity = serviceRepository.findById(serviceId).orElseThrow(() -> new InvalidInputException("serviceId"));
        return findAllTerminalServiceAccesses()
                .stream()
                .filter(serviceAccess -> serviceAccess.getService().getId().equals(serviceEntity.getId()))
                .map(TerminalServiceAccess::getTerminal)
                .collect(Collectors.toList());
    }

    @Override
    public Terminal craeteTerminal(TerminalCreateRequest request) {
        //normalize input
        request.setCode(request.getCode().toUpperCase());
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
        Terminal model = terminalMapper.toModel(terminalRepository.save(entity));
        evictCache();
        return model;
    }

    @Override
    public void deleteTerminal(TerminalDeleteRequest request) {
        terminalRepository
                .findById(request.getId())
                .ifPresentOrElse(found -> {
                    terminalRepository.delete(found);
                    evictCache();
                }, () -> {
                    throw new RecordVersionException("terminal");
                });
    }


    @Override
    public Terminal editTerminal(TerminalEditRequest request) {
        Terminal found = findTerminalById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("terminalId"));
        mapToTerminalEntity(found, request);
        terminalRepository.save(terminalMapper.toEntity(found));
        evictCache();
        return found;
    }

    private void mapToTerminalEntity(Terminal terminal, TerminalEditRequest request) {
        terminal.setTitle(request.getTitle());
        terminal.setStatus(request.getStatus());
        terminal.setSupportCheckAssetAccess(request.isSupportCheckAssetAccess());
        terminal.setSupportCheckServiceAccess(request.isSupportCheckServiceAccess());
        terminal.setSupportCheckAuthentication(request.isSupportCheckAuthentication());
        terminal.setSupportCheckSecondAuthentication(request.isSupportCheckSecondAuthentication());
        terminal.setSupportCustomerInjection(request.isSupportCustomerInjection());
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
                        throw new InvalidInputException("code (could not found legacy terminal id)");
                    }
                    //if the method could not find id it means the terminal code
                    //was wrong.
                    return legacyTerminalId;
                });

    }

    @Override
    @LegacyChannelManger
    public List<LegacyTerminal> findAllLegacyTerminal() {
        if (LEGACY_TERMINALS.isEmpty()) {
            synchronized (LEGACY_TERMINALS) {
                if (LEGACY_TERMINALS.isEmpty()) {
                    //language=sql
                    String query = "select CHANNEL_ID,CODE,NAME from REF.CHANNEL where ACTIVE = '1' and PUBLISHED = '1' ";
                    LEGACY_TERMINALS.addAll(jdbcTemplate.query(query, (rs, rowNum) -> {
                        LegacyTerminal legacyTerminal = new LegacyTerminal();
                        legacyTerminal.setId(rs.getInt("CHANNEL_ID"));
                        legacyTerminal.setCode(StringUtils.trim(rs.getString("CODE")).toString());
                        legacyTerminal.setName(StringUtils.trim(rs.getString("NAME")).toString());
                        return legacyTerminal;
                    }));
                }
            }
        }
        return LEGACY_TERMINALS;
    }

}
