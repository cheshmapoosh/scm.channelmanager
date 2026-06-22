package ir.daneshrefah.scm.cache.service;

import ir.daneshrefah.scm.cache.domain.config.InstanceConfigEntity;
import ir.daneshrefah.scm.cache.repository.InstanceCacheConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstanceCacheConfigService {
    private final InstanceCacheConfigRepository repository;

    @Transactional(readOnly = true)
    public List<InstanceConfigEntity> loadAll() {
        return repository.findAll();
    }
}
