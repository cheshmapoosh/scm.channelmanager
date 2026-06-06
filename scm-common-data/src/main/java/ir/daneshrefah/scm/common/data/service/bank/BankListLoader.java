package ir.daneshrefah.scm.common.data.service.bank;

import ir.daneshrefah.scm.common.data.dto.bank.BankDto;
import ir.daneshrefah.scm.common.data.entity.bank.BankEntity;
import ir.daneshrefah.scm.common.data.mapper.BankMapper;
import ir.daneshrefah.scm.common.data.repository.bank.BankRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class BankListLoader {
    private final BankRepository bankRepository;
    private final BankMapper bankMapper;
    private final CacheManager cacheManager;

    public static final String CACHE_BANK = "cache_bank";


    private Cache bankCache() {
        Cache cache = cacheManager.getCache(CACHE_BANK);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + CACHE_BANK);
        }
        return cache;
    }

    @PostConstruct
    public void putAllBankToCache() {
        Cache bankCache = bankCache();

        List<BankEntity> banks = bankRepository.findAll();
        for (BankEntity bank : banks) {
            String[] cardPreFixes = bank.getIin().split(",");
            for (String cardPrefix : cardPreFixes) {
                bankCache.put(cardPrefix, bankMapper.toDto(bank));
            }
        }
    }

    public BankDto getBank(String cardPreFix) {
        Cache bankCache = bankCache();
        return (BankDto) bankCache.get(cardPreFix).get();
    }
}
