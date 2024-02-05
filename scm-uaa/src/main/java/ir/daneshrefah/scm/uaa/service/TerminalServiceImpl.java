package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.common.data.mapper.TerminalMapper;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.TerminalInfoRequest;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private List<Terminal> terminals;

    @Override
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

    @Override
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

    @Override
    public List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId) {
        throw new RuntimeException("this method is not support.");
    }

    @Override
    public Optional<TerminalServiceAccess> findTerminalServiceAccessByTerminalCodeAndServiceCode(String terminalCode, String serviceCode) {
        throw new RuntimeException("this method is not support.");
    }

    @Override
    public TerminalServiceAccess assignServiceToTerminal(String terminalId, String serviceId) {
        throw new RuntimeException("this method is not support.");
    }
}
