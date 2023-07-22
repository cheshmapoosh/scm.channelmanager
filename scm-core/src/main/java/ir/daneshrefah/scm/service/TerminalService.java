package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.entity.terminal.TerminalServiceChannelAccessEntity;
import ir.daneshrefah.scm.mapper.TerminalMapper;
import ir.daneshrefah.scm.mapper.TerminalServiceAccessMapper;
import ir.daneshrefah.scm.mapper.TerminalServiceChannelAccessMapper;
import ir.daneshrefah.scm.repository.TerminalRepository;
import ir.daneshrefah.scm.repository.TerminalServiceChannelAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
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
        List<TerminalServiceChannelAccess> result = new ArrayList<>();
        for (Iterator<TerminalServiceChannelAccessEntity> iterator = entityList.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccessEntity entity = iterator.next();
            TerminalServiceChannelAccess model = new TerminalServiceChannelAccess();
            model.setId(entity.getId());
            model.setTerminalServiceAccess(TerminalServiceAccessMapper.INSTANCE.toModel(entity.getTerminalServiceAccessEntity()));
            model.getTerminalServiceAccess().setTerminal(TerminalMapper.INSTANCE.toModel(entity.getTerminalServiceAccessEntity().getTerminalEntity()));
//            model.setChannel();
            result.add(model);
        }
        return result;
//        return TerminalServiceChannelAccessMapper.INSTANCE.entitiesToModels(entityList);
    }
}
