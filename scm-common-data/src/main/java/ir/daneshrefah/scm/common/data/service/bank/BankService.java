package ir.daneshrefah.scm.common.data.service.bank;

import ir.daneshrefah.scm.common.data.mapper.BankMapper;
import ir.daneshrefah.scm.common.data.repository.bank.BankRepository;
import ir.daneshrefah.scm.common.data.dto.bank.BankDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class BankService {
    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    @Transactional(readOnly = true)
    @Cacheable("Bank")
    public Optional<BankDto> findOne(Long id) {
        log.debug("Request to get Bank : {}", id);
        return bankRepository.findById(id)
                .map(bankMapper::toDto);
    }
}
