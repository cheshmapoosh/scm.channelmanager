package ir.daneshrefah.scm.common.data.service.bundle;

import ir.daneshrefah.scm.common.constant.BundleParameterPattern;
import ir.daneshrefah.scm.common.data.mapper.ResourceBundleMapper;
import ir.daneshrefah.scm.common.data.repository.ResourceBundleRepository;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static ir.daneshrefah.scm.common.constant.BundleDefaults.INNER_REFERENCE_PREFIX;
import static ir.daneshrefah.scm.common.constant.BundleDefaults.INNER_REFERENCE_SUFFIX;

@Service
@AllArgsConstructor
@Slf4j
@Primary
public class CacheableResourceBundleService implements ResourceBundleService {
    private static final List<ir.daneshrefah.scm.common.model.bundle.ResourceBundle> RESOURCE_BUNDLE_CACHE = new ArrayList<>(100);
    private static final Locale DEFAULT_LOCALE = new Locale("en", "US");
    private static CacheableResourceBundleService INSTANCE;
    private final ResourceBundleRepository resourceBundleRepository;

    @PostConstruct
    public void init() {
        resourceBundleRepository
                .findAll()
                .stream()
                .map(ResourceBundleMapper.INSTANCE::toModel)
                .forEach(RESOURCE_BUNDLE_CACHE::add);
        log.info(">>> All {} resource bundles cached from database", RESOURCE_BUNDLE_CACHE.size());
        INSTANCE = this;
    }


    @Override
    public Optional<String> get(Locale locale, String key) {
        return findCache(locale, key).map(ResourceBundle::getValue)
                .map(value -> {
                    if (value.contains(INNER_REFERENCE_PREFIX)) {
                        value = autoReplaceInnerReference(value, locale);
                    }
                    return value;
                });
    }

    private String autoReplaceInnerReference(String value, Locale locale) {
        int refCount = StringUtils.countMatches(value, INNER_REFERENCE_PREFIX);
        for (int i = 0; i < refCount; i++) {
            String ref = value.substring(value.indexOf(INNER_REFERENCE_PREFIX), value.indexOf(INNER_REFERENCE_SUFFIX + INNER_REFERENCE_SUFFIX.length()));
            String refKey = ref.replace(INNER_REFERENCE_PREFIX, StringUtils.EMPTY).replace(INNER_REFERENCE_SUFFIX, StringUtils.EMPTY);
            value = value.replaceFirst(ref, get(locale, refKey).orElse(StringUtils.EMPTY));
        }
        return value;
    }

    @Override
    public Optional<String> get(String key) {
        return get(DEFAULT_LOCALE, key);
    }

    @Override
    public Optional<String> get(String key, BundleParameterPattern parameterPattern, Object... parameters) {
        return get(DEFAULT_LOCALE, key, parameterPattern, parameters);
    }

    /**
     * @apiNote <pre>
     * If BundleParameterPattern is STRING_FORMAT or BRACKET_SERIES the input parameters assigned as String, otherwise if the pattern is KEY_ASSIGNMENT the input Parameters must be as a HashMap<String,Sting>
     * </pre>
     */
    @Override
    public Optional<String> get(Locale locale, String key, BundleParameterPattern parameterPattern, Object... parameters) {
        return get(locale, key).map(bundleMsg -> assignParameters(bundleMsg, parameterPattern, parameters));
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
    public void update(ResourceBundle resourceBundle) {
        resourceBundleRepository
                .findByLocaleAndKey(resourceBundle.getLocale(), resourceBundle.getKey())
                .ifPresent(found -> {
                    found.setValue(resourceBundle.getValue());
                    found.setLastEditDate(LocalDateTime.now());
                    findCache(resourceBundle.getLocale(), resourceBundle.getKey()).ifPresent(bundle -> {
                        bundle.setValue(found.getValue());
                        bundle.setLastEditDate(found.getLastEditDate());
                    });
                    resourceBundleRepository.save(found);
                });
    }

    @Override
    public List<ResourceBundle> getAll() {
        return RESOURCE_BUNDLE_CACHE;
    }

    public static ResourceBundleService getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private String assignParameters(String bundleMsg, BundleParameterPattern parameterPattern, Object... parameters) {
        if (Objects.isNull(parameters) || parameters.length == 0) {
            return bundleMsg;
        }
        if (parameters[0] instanceof Map<?, ?> map && map.keySet().isEmpty()) {
            return bundleMsg;
        }
        return switch (parameterPattern) {
            case STRING_FORMAT -> {
                for (Object parameter : parameters) {
                    bundleMsg = bundleMsg.replaceFirst("%s", String.valueOf(parameter));
                }
                yield bundleMsg;
            }
            case BRACKET_SERIES -> {
                int counter = 0;
                for (Object parameter : parameters) {
                    bundleMsg = bundleMsg.replace("{" + counter + "}", String.valueOf(parameter));
                    counter++;
                }
                yield bundleMsg;
            }
            case KEY_ASSIGNMENT -> {
                if (Objects.nonNull(parameters[0]) && parameters[0] instanceof Map<?, ?>) {
                    Map<String, String> keyValueParams = (Map<String, String>) parameters[0];
                    Set<String> keys = keyValueParams.keySet();
                    for (String key : keys) {
                        bundleMsg = bundleMsg.replace(":" + key, keyValueParams.get(key));
                    }
                }
                yield bundleMsg;
            }
        };
    }

    private Optional<ResourceBundle> findCache(Locale locale, String key) {
        return RESOURCE_BUNDLE_CACHE
                .stream()
                .filter(resourceBundle -> resourceBundle.getLocale().equals(locale))
                .filter(resourceBundle -> resourceBundle.getKey().equals(key))
                .findFirst();
    }

}
