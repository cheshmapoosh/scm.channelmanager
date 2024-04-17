package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.data.entity.terminal.LegacyTerminalEntity;
import ir.daneshrefah.scm.common.data.repository.LegacyTerminalRepository;
import ir.daneshrefah.scm.common.model.terminal.LegacyTerminal;
import ir.daneshrefah.scm.core.mapper.LegacyTerminalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LegacyTerminalService {
    private final LegacyTerminalRepository legacyTerminalRepository;

    public Optional<LegacyTerminal> findByCodeWithOutParent(String code){
        return legacyTerminalRepository
                .findByCodeAndParentId(code, null)
                .map(LegacyTerminalMapper.INSTANCE::toModel);

    }
}
