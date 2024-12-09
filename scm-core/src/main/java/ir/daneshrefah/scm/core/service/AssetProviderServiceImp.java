package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.service.AssetProviderService;
import ir.daneshrefah.scm.core.mapper.AssetProviderMapper;
import ir.daneshrefah.scm.core.repository.AssetProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetProviderServiceImp implements AssetProviderService {

    private final AssetProviderRepository assetProviderRepository;
    private List<AssetProvider> assetProviders;

    public void evictCache() {
        synchronized (this) {
            if (Objects.nonNull(assetProviders)) {
                assetProviders.clear();
            }
        }
    }

    @Override
    public List<AssetProvider> findAssetProviderList() {
        if (null == assetProviders || assetProviders.isEmpty()) {
            synchronized (this) {
                assetProviders = AssetProviderMapper.INSTANCE.toModels(assetProviderRepository.findAll());
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
