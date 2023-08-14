package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.entity.authority.AuthorityEntity;
import ir.daneshrefah.scm.core.entity.authority.TerminalServiceAccessAuthorityEntity;
import ir.daneshrefah.scm.core.entity.authority.TerminalWithdrawAuthorityEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceChannelAccessEntity;
import ir.daneshrefah.scm.core.mapper.AuthorityMapper;
import ir.daneshrefah.scm.core.mapper.TerminalMapper;
import ir.daneshrefah.scm.core.mapper.TerminalServiceChannelAccessMapper;
import ir.daneshrefah.scm.core.repository.AuthorityRepository;
import ir.daneshrefah.scm.core.repository.TerminalRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceChannelAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TerminalService {

    @Autowired
    TerminalRepository terminalRepository;
    @Autowired
    AuthorityRepository authorityRepository;
    @Autowired
    TerminalServiceChannelAccessRepository terminalServiceChannelAccessRepository;

    public List<Terminal> findAllTerminals() {
        return TerminalMapper.INSTANCE.entitiesToModels(terminalRepository.findAll());
    }

    public List<TerminalServiceChannelAccess> findTerminalServiceChannelAccessByChannelId(String channelId) {
        List<TerminalServiceChannelAccessEntity> entityList = terminalServiceChannelAccessRepository.findAllByChannelEntityId(channelId);
        return TerminalServiceChannelAccessMapper.INSTANCE.entitiesToModels(entityList);
    }

    public List<Authority> findAllTerminalAuthorities() {
        List<Class<? extends AuthorityEntity>> entityTypes = Arrays.asList(TerminalServiceAccessAuthorityEntity.class, TerminalWithdrawAuthorityEntity.class);
        List<AuthorityEntity> authorityEntities = authorityRepository.findByDiscriminators(entityTypes);
        return AuthorityMapper.INSTANCE.toAuthorities(authorityEntities);
    }

}
