package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceChannelAccessEntity;
import ir.daneshrefah.scm.core.mapper.TerminalMapper;
import ir.daneshrefah.scm.core.mapper.TerminalServiceChannelAccessMapper;
import ir.daneshrefah.scm.core.repository.TerminalRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceChannelAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TerminalService {

    @Autowired
    TerminalRepository terminalRepository;
    @Autowired
    TerminalServiceChannelAccessRepository terminalServiceChannelAccessRepository;

    public List<Terminal> findAllTerminals() {
        return TerminalMapper.INSTANCE.entitiesToModels(terminalRepository.findAll());
    }

    public List<TerminalServiceChannelAccess> findTerminalServiceChannelAccessByChannelId(String channelId) {
        List<TerminalServiceChannelAccessEntity> entityList = terminalServiceChannelAccessRepository.findAllByChannelEntityId(channelId);
        return TerminalServiceChannelAccessMapper.INSTANCE.entitiesToModels(entityList);
    }

}
