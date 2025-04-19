package ir.daneshrefah.scm.core.services;

import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.service.AssetProviderService;
import ir.daneshrefah.scm.core.mapper.AssetProviderMapper;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.common.data.repository.assets.AssetProviderRepository;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.plugin.api.config.AssetProviderConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetProviderServiceImp implements AssetProviderService {

    private final AssetProviderRepository assetProviderRepository;
    private final ServiceRepository serviceRepository;
    private final AssetProviderConfigProperties providerConfigProperties;
    private List<AssetProvider> assetProviders = new ArrayList<>();

    public void evictCache() {
        synchronized (this) {
            assetProviders.clear();
        }
    }

    @Override
    public List<AssetProvider> findAssetProviderList() {
        if (assetProviders.isEmpty()) {
            synchronized (this) {
                if (assetProviders.isEmpty()) {
                    assetProviders = assetProviderRepository
                            .findAll()
                            .stream()
                            .map(assetProviderEntity -> {
                                AssetProvider assetProvider = AssetProviderMapper.INSTANCE.toModel(assetProviderEntity);
                                //TODO ROLLBACK THIS COMMENT AFTER ADD COLUMN 'NEXT RELEASE'
//                                if (Objects.nonNull(assetProviderEntity.getServiceId())) {
//                                    assetProvider.setService(ServiceMapper.INSTANCE.toService(serviceRepository.findById(assetProviderEntity.getServiceId()).orElse(null)));
//                                } else
                                if (providerConfigProperties.getConfigs().containsKey(assetProvider.getCode().getValue())) {
                                    AssetProviderConfigProperties.AssetProviderConfig assetProviderConfig = providerConfigProperties.getConfigs().get(assetProvider.getCode().getValue());
                                    assetProvider.setService(ServiceMapper.INSTANCE.toService(serviceRepository.findByCode(assetProviderConfig.getProviderServiceCode()).orElse(null)));
                                }
                                return assetProvider;
                            })
                            .collect(Collectors.toList());
                }
            }
        }
        return assetProviders;
    }

    @Override
    public Optional<AssetProvider> findAssetProviderById(Integer id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return findAssetProviderList().stream().filter(assetProvider -> id.equals(assetProvider.getId())).findFirst();
    }

}
