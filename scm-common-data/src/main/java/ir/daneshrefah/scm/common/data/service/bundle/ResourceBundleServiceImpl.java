package ir.daneshrefah.scm.common.data.service.bundle;

import ir.daneshrefah.scm.common.data.entity.bundle.ResourceBundleEntity;
import ir.daneshrefah.scm.common.data.mapper.ResourceBundleMapper;
import ir.daneshrefah.scm.common.data.repository.ResourceBundleRepository;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;
import ir.daneshrefah.scm.common.model.message.Authentication;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ResourceBundleServiceImpl implements ResourceBundleService {
    private static final List<ir.daneshrefah.scm.common.model.bundle.ResourceBundle> RESOURCE_BUNDLE_CACHE = new ArrayList<>(100);
    private static final Locale DEFAULT_LOCALE = new Locale("en", "US");
    private final ResourceBundleRepository resourceBundleRepository;

    @PostConstruct
    public void init() {
        resourceBundleRepository
                .findAll()
                .stream()
                .map(ResourceBundleMapper.INSTANCE::toModel)
                .forEach(RESOURCE_BUNDLE_CACHE::add);
        log.info(">>> All {} resource bundles cached from database", RESOURCE_BUNDLE_CACHE.size());
    }


    @Override
    public Optional<String> get(Locale locale, String key) {
        return findCache(locale, key).map(ResourceBundle::getValue);
    }

    @Override
    public Optional<String> get(String key) {
        return findCache(DEFAULT_LOCALE, key).map(ResourceBundle::getValue);
    }

    @Override
    public boolean contains(Locale locale, String key) {
        return get(locale, key).isPresent();
    }

    @Override
    public boolean contains(String key) {
        return get(key).isPresent();
    }

    @Override
    public void put(ResourceBundle resourceBundle) {
        resourceBundleRepository
                .findByLocaleAndKey(resourceBundle.getLocale(), resourceBundle.getKey())
                .ifPresentOrElse(found -> {
                    found.setValue(resourceBundle.getValue());
                    findCache(resourceBundle.getLocale(), resourceBundle.getKey()).ifPresent(bundle -> {
                        bundle.setValue(resourceBundle.getValue());
                    });
                    found.setLastEditDate(LocalDateTime.now());
                    resourceBundleRepository.save(found);
                }, () -> {
                    resourceBundle.setCreateDate(LocalDateTime.now());
                    resourceBundle.setLastEditDate(LocalDateTime.now());
                    ResourceBundleEntity saved = resourceBundleRepository.save(ResourceBundleMapper.INSTANCE.toEntity(resourceBundle));
                    synchronized (this) {
                        RESOURCE_BUNDLE_CACHE.add(ResourceBundleMapper.INSTANCE.toModel(saved));
                    }
                });
    }

    private Optional<ResourceBundle> findCache(Locale locale, String key) {
        return RESOURCE_BUNDLE_CACHE
                .stream()
                .filter(resourceBundle -> resourceBundle.getLocale().equals(locale))
                .filter(resourceBundle -> resourceBundle.getKey().equals(key))
                .findFirst();
    }

}
