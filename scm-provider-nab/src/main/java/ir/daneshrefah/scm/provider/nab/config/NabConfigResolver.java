package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderConfigurationBinder;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldType;
import ir.daneshrefah.scm.provider.nab.domain.NabOverflowPolicy;
import ir.daneshrefah.scm.provider.nab.domain.NabPadding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class NabConfigResolver {
    public static final String COMPONENT_SCHEME = "scm-nab";

    private final ProviderRegistryProperties providerRegistryProperties;
    private final ProviderMessageCustomizerPipelineFactory pipelineFactory;
    private final ConcurrentMap<String, NabResolvedConfig> resolvedConfigs = new ConcurrentHashMap<>();

    public NabConfigResolver(
            ProviderRegistryProperties providerRegistryProperties,
            ProviderMessageCustomizerPipelineFactory pipelineFactory
    ) {
        this.providerRegistryProperties = providerRegistryProperties == null
                ? new ProviderRegistryProperties()
                : providerRegistryProperties;
        this.pipelineFactory = Objects.requireNonNull(pipelineFactory, "ProviderMessageCustomizerPipelineFactory is required");
    }

    public NabResolvedConfig resolve(String provider, NabEndpointOverrides overrides) {
        ProviderReference reference = normalizeProviderReference(provider);
        if (reference == null) {
            throw new IllegalArgumentException("NAB provider is required");
        }
        NabResolvedConfig base = resolvedConfigs.computeIfAbsent(cacheKey(reference.providerCode()), ignored -> resolveBase(reference));
        return base.withOverrides(overrides);
    }

    public String providerName(String provider) {
        ProviderReference reference = normalizeProviderReference(provider);
        return reference == null ? null : reference.providerCode();
    }

    public List<String> availableProviderCodes() {
        return providerRegistryProperties.entrySet().stream()
                .filter(entry -> entry.getKey() != null && isNabProvider(entry.getValue()))
                .map(Map.Entry::getKey)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private NabResolvedConfig resolveBase(ProviderReference reference) {
        RegistryEntry entry = findProvider(reference.providerCode(), reference.scheme());
        NabProviderInstanceProperties instance = ProviderConfigurationBinder.bind(
                entry.properties(), NabProviderInstanceProperties.class, "NAB provider " + entry.providerCode());
        validate(entry.providerCode(), reference.scheme(), instance);
        String protocol = StringUtils.trimToNull(instance.getProtocol()).toUpperCase(Locale.ROOT);
        int rqUidLength = value(instance.getRqUid().getLength(), 16);
        Map<String, List<NabFieldSpec>> headerFields = resolveHeaderFields(instance, protocol);
        ProviderMessageCustomizerContext customizerContext = new ProviderMessageCustomizerContext(
                entry.providerCode(), COMPONENT_SCHEME, providerUri(COMPONENT_SCHEME, entry.providerCode()),
                null, null, null, Map.of(), instance, null, null);
        ProviderMessageCustomizerPipeline pipeline = pipelineFactory.build(customizerContext, instance.getMessageCustomizers());
        log.debug("NAB provider runtime resolved provider={} customizers={}", entry.providerCode(),
                pipeline.entries().stream().map(item -> item.type() + "#" + item.order()).toList());
        return new NabResolvedConfig(
                entry.providerCode(),
                COMPONENT_SCHEME,
                StringUtils.trimToNull(instance.getEndpoint()),
                protocol,
                value(instance.getConnectTimeoutMs(), 3000),
                value(instance.getSocketTimeoutMs(), 1000),
                value(instance.getResponseTimeoutMs(), 6000),
                value(instance.getResponseIdleTimeoutMs(), 100),
                value(instance.getAckLengthBytes(), 5),
                StringUtils.defaultIfBlank(instance.getCharset(), "windows-1252"),
                instance.getUserId(),
                instance.getPassword(),
                StringUtils.defaultIfBlank(instance.getDefaultServiceCode(), "99"),
                Map.copyOf(nonNullMap(instance.getServiceCodesByTerminalType())),
                Map.copyOf(nonNullMap(instance.getServiceCodesByChannelCode())),
                headerFields,
                pipeline,
                resolvedRateLimit(instance.getRateLimit(), entry.providerCode()),
                new NabResolvedConfig.RqUid(rqUidLength, StringUtils.defaultIfBlank(instance.getRqUid().getType(), "NUMERIC")),
                new NabResolvedConfig.CharacterNormalization(
                        Boolean.TRUE.equals(instance.getCharacterNormalization().getEnabled()),
                        Map.copyOf(nonNullMap(instance.getCharacterNormalization().getReplacements()))
                ),
                booleanValue(instance.getWireLogEnabled(), true)
        );
    }

    private void validate(String providerName, String uriScheme, NabProviderInstanceProperties instance) {
        String configuredScheme = StringUtils.trimToNull(instance.getScheme());
        if (configuredScheme == null) {
            throw new IllegalArgumentException("Provider " + providerName + " must define scheme=" + COMPONENT_SCHEME);
        }
        if (!COMPONENT_SCHEME.equalsIgnoreCase(configuredScheme)) {
            throw new IllegalArgumentException("Provider URI scheme mismatch for provider '" + providerName
                    + "'. URI scheme is '" + uriScheme + "' but configured scheme is '" + configuredScheme + "'.");
        }
        if (Boolean.FALSE.equals(instance.getEnabled())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " is disabled");
        }
        if (StringUtils.isBlank(instance.getProtocol())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define protocol");
        }
        if (StringUtils.isBlank(instance.getEndpoint())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define endpoint");
        }
        if (StringUtils.isBlank(instance.getUserId())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define user-id");
        }
        if (StringUtils.isBlank(instance.getPassword())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define password or credential resolver");
        }
    }

    private RegistryEntry findProvider(String providerName, String scheme) {
        Map<String, Object> exact = providerRegistryProperties.provider(providerName);
        if (exact != null) {
            return new RegistryEntry(providerName, exact);
        }
        return providerRegistryProperties.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(entry -> new RegistryEntry(entry.getKey(), entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Provider '" + providerName
                        + "' with scheme '" + scheme + "' is not configured. Available providers for scheme '" + scheme + "': "
                        + availableProviderCodes()));
    }

    private ProviderReference normalizeProviderReference(String provider) {
        String providerName = StringUtils.trimToNull(provider);
        if (providerName == null) {
            return null;
        }
        int separator = providerName.indexOf(':');
        if (separator < 0) {
            return new ProviderReference(providerName, COMPONENT_SCHEME, providerUri(COMPONENT_SCHEME, providerName));
        }
        String scheme = providerName.substring(0, separator).trim();
        String name = providerName.substring(separator + 1).trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Invalid NAB provider name: " + providerName);
        }
        if (!COMPONENT_SCHEME.equalsIgnoreCase(scheme)) {
            throw unsupportedScheme(scheme);
        }
        return new ProviderReference(name, scheme, providerUri(scheme, name));
    }

    private NabResolvedConfig.RateLimit resolvedRateLimit(
            NabProviderInstanceProperties.RateLimit rateLimit,
            String providerName
    ) {
        NabProviderInstanceProperties.RateLimit safe = rateLimit == null ? new NabProviderInstanceProperties.RateLimit() : rateLimit;
        boolean enabled = Boolean.TRUE.equals(safe.getEnabled());
        String bucket = StringUtils.trimToNull(safe.getBucket());
        if (enabled && bucket == null) {
            throw new IllegalArgumentException("NAB provider " + providerName + " rate-limit.bucket is required when rate-limit is enabled");
        }
        String key = StringUtils.defaultIfBlank(safe.getKey(), "provider-operation");
        return new NabResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private Map<String, List<NabFieldSpec>> resolveHeaderFields(
            NabProviderInstanceProperties instance,
            String configuredProtocol
    ) {
        Map<String, List<NabFieldSpec>> result = new LinkedHashMap<>();
        Map<String, List<NabProviderInstanceProperties.Field>> configured = nonNullMap(instance.getHeaderFieldsByProtocol());
        configured.forEach((protocol, fields) -> result.put(protocol.toUpperCase(Locale.ROOT), toFieldSpecs(fields, "header." + protocol)));
        List<NabProviderInstanceProperties.Field> instanceHeaderFields = nonNullList(instance.getHeaderFields());
        if (!instanceHeaderFields.isEmpty()) {
            result.put(configuredProtocol, toFieldSpecs(instanceHeaderFields, "header"));
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("NAB provider header-fields must be configured per provider instance");
        }
        return Map.copyOf(result);
    }

    private List<NabFieldSpec> toFieldSpecs(List<NabProviderInstanceProperties.Field> fields, String owner) {
        List<NabFieldSpec> result = new ArrayList<>();
        for (NabProviderInstanceProperties.Field field : fields == null ? List.<NabProviderInstanceProperties.Field>of() : fields) {
            result.add(toFieldSpec(field, owner));
        }
        return List.copyOf(result);
    }

    private NabFieldSpec toFieldSpec(NabProviderInstanceProperties.Field field, String owner) {
        if (field == null) {
            throw new IllegalArgumentException(owner + " field spec must not be null");
        }
        String name = StringUtils.trimToNull(field.getName());
        if (name == null) {
            throw new IllegalArgumentException(owner + " field spec must define name");
        }
        if (field.getLength() == null || field.getLength() < 1) {
            throw new IllegalArgumentException(owner + " field " + name + " must define positive length");
        }
        return new NabFieldSpec(name, StringUtils.defaultIfBlank(field.getPath(), pointerForName(name)), field.getLength(),
                NabFieldType.from(field.getType()), Boolean.TRUE.equals(field.getRequired()),
                StringUtils.defaultIfBlank(field.getConverter(), "NONE"), NabPadding.from(field.getPadding()),
                NabOverflowPolicy.from(field.getOverflow()), field.getTrim() == null || field.getTrim());
    }

    private String pointerForName(String name) {
        return "/" + name;
    }

    private String cacheKey(String providerName) {
        return providerName.toLowerCase(Locale.ROOT);
    }

    private boolean isNabProvider(Map<String, Object> properties) {
        if (properties == null) {
            return false;
        }
        Object scheme = properties.get("scheme");
        return scheme != null && COMPONENT_SCHEME.equalsIgnoreCase(String.valueOf(scheme));
    }

    private IllegalArgumentException unsupportedScheme(String scheme) {
        return new IllegalArgumentException("Unsupported SCM provider scheme '" + scheme
                + "'. Use '" + COMPONENT_SCHEME + ":<providerCode>' instead.");
    }

    private String providerUri(String scheme, String providerCode) {
        return scheme + ":" + providerCode;
    }

    private static int value(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private static boolean booleanValue(Boolean value, boolean fallback) {
        return value != null ? value : fallback;
    }

    private static <T> List<T> nonNullList(List<T> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private static <K, V> Map<K, V> nonNullMap(Map<K, V> value) {
        return value == null ? Map.of() : value;
    }

    private record RegistryEntry(String providerCode, Map<String, Object> properties) {
    }

    private record ProviderReference(String providerCode, String scheme, String providerUri) {
    }
}
