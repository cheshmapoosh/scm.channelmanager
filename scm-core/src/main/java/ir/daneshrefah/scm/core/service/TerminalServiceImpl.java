package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.TerminalMapper;
import ir.daneshrefah.scm.core.mapper.TerminalServiceAccessMapper;
import ir.daneshrefah.scm.core.repository.TerminalRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TerminalServiceImpl implements TerminalService {

    @Autowired
    TerminalRepository terminalRepository;
    @Autowired
    TerminalServiceAccessRepository terminalServiceChannelAccessRepository;

    public List<Terminal> findAllTerminals() {
        return TerminalMapper.INSTANCE.entitiesToModels(terminalRepository.findAll());
    }

    public List<TerminalServiceAccess> findTerminalServiceAccessByTerminalId(String terminalId) {
        List<TerminalServiceAccessEntity> entityList = terminalServiceChannelAccessRepository.findAllByTerminalId(terminalId);
        return TerminalServiceAccessMapper.INSTANCE.entitiesToModels(entityList);
    }

}
