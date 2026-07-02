package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.data.repository.assets.AccountTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

//@Component
@RequiredArgsConstructor
public class AccountTypeLoader {
    private final AccountTypeRepository accountTypeRepository;

    public static Map<String, AccountTypeEntity> accountTypeEntityMap = new HashMap<>();

    @PostConstruct
    public void loadAllAccountType(){
        accountTypeRepository.findAll().forEach(accountTypeEntity -> {
            accountTypeEntityMap.put(accountTypeEntity.getCode(), accountTypeEntity);
        });
    }
}
