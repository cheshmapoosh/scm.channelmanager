package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.model.asset.AssetProvider;

import java.util.List;
import java.util.Optional;

public interface AssetProviderService {
    List<AssetProvider> findAssetProviderList();
    Optional<AssetProvider> findAssetProviderById(Integer id);
    void evictCache();
}
