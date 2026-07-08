package ir.daneshrefah.scm.core.integration.service.lookup;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.core.repository.service.ScmServiceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EbServiceLookupService {
    public static final String CACHE_NAME = "ebServiceByCode";

    private final ScmServiceRepository scmServiceRepository;

    @Cacheable(cacheNames = CACHE_NAME, key = "#code")
    @Transactional(readOnly = true)
    public EbServiceSnapshot getActiveServiceByCode(String code) {
        String serviceCode = StringUtils.trimToNull(code);
        if (serviceCode == null) {
            return null;
        }
        return scmServiceRepository.findByCode(serviceCode)
                .map(this::toSnapshot)
                .orElse(null);
    }

    private EbServiceSnapshot toSnapshot(ServiceEntity entity) {
        return new EbServiceSnapshot(
                entity.getId() == null ? null : entity.getId().longValue(),
                entity.getCode(),
                entity.getRoutingStrategy(),
                Boolean.TRUE.equals(entity.getPublish())
        );
    }
}
