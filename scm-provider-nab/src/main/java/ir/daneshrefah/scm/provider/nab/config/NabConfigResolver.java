package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
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
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class NabConfigResolver {
    private final ProviderRegistryProperties providerRegistryProperties;
    private final NabProperties legacyProperties;
    private final AtomicBoolean legacyWarningLogged = new AtomicBoolean();

    public NabConfigResolver(
            ProviderRegistryProperties providerRegistryProperties,
            NabProperties legacyProperties
    ) {
        this.providerRegistryProperties = providerRegistryProperties == null
                ? new ProviderRegistryProperties()
                : providerRegistryProperties;
        this.legacyProperties = legacyProperties == null ? new NabProperties() : legacyProperties;
    }

    public NabConfigResolver(NabProperties legacyProperties) {
        this(new ProviderRegistryProperties(), legacyProperties);
    }

    public NabResolvedConfig resolve(String provider, NabEndpointOverrides overrides) {
        String providerName = normalizeProviderName(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("NAB provider is required");
        }
        ProviderRegistryProperties.Provider unified = findUnifiedProvider(providerName);
        if (unified != null) {
            return resolveUnified(providerName, unified, overrides);
        }
        return resolveLegacy(providerName, overrides);
    }

    private NabResolvedConfig resolveUnified(
            String providerName,
            ProviderRegistryProperties.Provider instance,
            NabEndpointOverrides overrides
    ) {
        validateType(providerName, instance.getType());
        if (Boolean.FALSE.equals(instance.getEnabled())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " is disabled");
        }
        String endpoint = endpointFromUnified(providerName, instance);
        String protocol = StringUtils.trimToNull(instance.getProtocol());
        if (protocol == null) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define protocol");
        }
        protocol = protocol.toUpperCase(Locale.ROOT);
        if (StringUtils.isBlank(instance.getUserId())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define user-id");
        }
        if (StringUtils.isBlank(instance.getPassword())) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define password or credential resolver");
        }
        int rqUidLength = value(instance.getRqUid().getLength(), 16);
        Map<String, List<NabFieldSpec>> headerFields = resolveUnifiedHeaderFields(instance, rqUidLength, protocol);
        String charset = overrides != null && StringUtils.isNotBlank(overrides.charset())
                ? overrides.charset()
                : StringUtils.defaultIfBlank(instance.getCharset(), "windows-1252");
        int responseTimeoutMs = overrides != null && overrides.timeoutMs() != null
                ? overrides.timeoutMs()
                : value(instance.getResponseTimeoutMs(), 6000);

        return new NabResolvedConfig(
                providerName,
                "nab",
                endpoint,
                protocol,
                value(instance.getConnectTimeoutMs(), 3000),
                value(instance.getSocketTimeoutMs(), 1000),
                responseTimeoutMs,
                value(instance.getResponseIdleTimeoutMs(), 100),
                value(instance.getAckLengthBytes(), 5),
                charset,
                instance.getUserId(),
                instance.getPassword(),
                StringUtils.defaultIfBlank(instance.getDefaultServiceCode(), "99"),
                Map.copyOf(nonNullMap(instance.getServiceCodesByTerminalType())),
                Map.copyOf(nonNullMap(instance.getServiceCodesByChannelCode())),
                headerFields,
                resolvedRateLimit(instance.getRateLimit(), overrides, providerName),
                new NabResolvedConfig.RqUid(rqUidLength, StringUtils.defaultIfBlank(instance.getRqUid().getType(), "NUMERIC")),
                new NabResolvedConfig.CharacterNormalization(
                        Boolean.TRUE.equals(instance.getCharacterNormalization().getEnabled()),
                        Map.copyOf(nonNullMap(instance.getCharacterNormalization().getReplacements()))
                ),
                booleanValue(instance.getWireLogEnabled(), true)
        );
    }

    private NabResolvedConfig resolveLegacy(String providerName, NabEndpointOverrides overrides) {
        logLegacyWarning();
        NabProperties.Instance instance = findLegacyProvider(providerName);
        NabProperties.Instance defaults = legacyProperties.getDefaults();
        String endpoint = resolveEndpoint(providerName, defaults, instance);

        NabProperties.RqUid rqUid = mergeRqUid(defaults.getRqUid(), instance.getRqUid());
        NabProperties.CharacterNormalization normalization = mergeNormalization(defaults.getCharacterNormalization(), instance.getCharacterNormalization());
        NabProperties.RateLimit rateLimit = mergeRateLimit(defaults.getRateLimit(), instance.getRateLimit());
        String charset = overrides != null && StringUtils.isNotBlank(overrides.charset())
                ? overrides.charset()
                : first(instance.getCharset(), defaults.getCharset());
        int responseTimeoutMs = overrides != null && overrides.timeoutMs() != null
                ? overrides.timeoutMs()
                : value(first(instance.getResponseTimeoutMs(), defaults.getResponseTimeoutMs()), 6000);
        int rqUidLength = value(rqUid.getLength(), 16);
        String configuredProtocol = resolvedProtocol(instance, defaults);

        return new NabResolvedConfig(
                providerName,
                "nab",
                endpoint,
                configuredProtocol,
                value(first(instance.getConnectTimeoutMs(), defaults.getConnectTimeoutMs()), 3000),
                value(first(instance.getSocketTimeoutMs(), defaults.getSocketTimeoutMs()), 1000),
                responseTimeoutMs,
                value(first(instance.getResponseIdleTimeoutMs(), defaults.getResponseIdleTimeoutMs()), 100),
                value(first(instance.getAckLengthBytes(), defaults.getAckLengthBytes()), 5),
                StringUtils.defaultIfBlank(charset, "windows-1252"),
                first(instance.getUserId(), defaults.getUserId()),
                first(instance.getPassword(), defaults.getPassword()),
                StringUtils.defaultIfBlank(first(instance.getDefaultServiceCode(), defaults.getDefaultServiceCode()), "99"),
                mergeMap(defaults.getServiceCodesByTerminalType(), instance.getServiceCodesByTerminalType()),
                mergeMap(defaults.getServiceCodesByChannelCode(), instance.getServiceCodesByChannelCode()),
                resolveHeaderFields(defaults, instance, rqUidLength, configuredProtocol),
                resolvedLegacyRateLimit(rateLimit, overrides),
                new NabResolvedConfig.RqUid(rqUidLength, StringUtils.defaultIfBlank(rqUid.getType(), "NUMERIC")),
                new NabResolvedConfig.CharacterNormalization(
                        Boolean.TRUE.equals(normalization.getEnabled()),
                        Map.copyOf(nonNullMap(normalization.getReplacements()))
                ),
                booleanValue(first(instance.getWireLogEnabled(), defaults.getWireLogEnabled()), true)
        );
    }

    private String resolvedProtocol(NabProperties.Instance instance, NabProperties.Instance defaults) {
        String protocol = first(instance.getProtocol(), defaults.getProtocol());
        protocol = StringUtils.trimToNull(protocol);
        return protocol == null ? null : protocol.toUpperCase(Locale.ROOT);
    }

    private String resolveEndpoint(String providerName, NabProperties.Instance defaults, NabProperties.Instance instance) {
        String instanceEndpoint = endpointFromInstance(instance, "providers." + providerName);
        if (StringUtils.isNotBlank(instanceEndpoint)) {
            return instanceEndpoint;
        }
        String defaultEndpoint = endpointFromInstance(defaults, "defaults");
        if (StringUtils.isNotBlank(defaultEndpoint)) {
            return defaultEndpoint;
        }
        throw new IllegalArgumentException("NAB provider " + providerName + " must define endpoint (host:port)");
    }

    private String endpointFromUnified(String providerName, ProviderRegistryProperties.Provider instance) {
        String endpoint = StringUtils.trimToNull(instance.getEndpoint());
        List<String> endpoints = nonBlankValues(nonNullList(instance.getEndpoints()));
        if (endpoints.size() > 1) {
            throw new IllegalArgumentException("NAB provider " + providerName + ".endpoints supports only one value; use endpoint instead");
        }
        if (endpoint != null) {
            return endpoint;
        }
        if (!endpoints.isEmpty()) {
            return endpoints.getFirst();
        }
        throw new IllegalArgumentException("NAB provider " + providerName + " must define endpoint");
    }

    private String endpointFromInstance(NabProperties.Instance instance, String owner) {
        String endpoint = StringUtils.trimToNull(instance.getEndpoint());
        List<String> endpoints = nonBlankValues(nonNullList(instance.getEndpoints()));
        if (endpoints.size() > 1) {
            throw new IllegalArgumentException("NAB " + owner + ".endpoints supports only one value; use endpoint instead");
        }
        if (endpoint != null) {
            return endpoint;
        }
        return endpoints.isEmpty() ? null : endpoints.getFirst();
    }

    private String normalizeProviderName(String provider) {
        String providerName = StringUtils.trimToNull(provider);
        if (providerName == null) {
            return null;
        }
        int separator = providerName.indexOf(':');
        if (separator < 0) {
            return providerName;
        }
        String type = providerName.substring(0, separator).trim();
        String name = providerName.substring(separator + 1).trim();
        if (!"nab".equalsIgnoreCase(type) || name.isBlank()) {
            throw new IllegalArgumentException("Invalid NAB provider name: " + providerName + ". Expected nab:<name>");
        }
        return name;
    }

    private ProviderRegistryProperties.Provider findUnifiedProvider(String providerName) {
        Map<String, ProviderRegistryProperties.Provider> providers = providerRegistryProperties.getProviders() == null
                ? Map.of()
                : providerRegistryProperties.getProviders();
        ProviderRegistryProperties.Provider exact = providers.get(providerName);
        if (exact != null) {
            return exact;
        }
        return providers.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private NabProperties.Instance findLegacyProvider(String providerName) {
        Map<String, NabProperties.Instance> providers = legacyProperties.getProviders() == null ? Map.of() : legacyProperties.getProviders();
        NabProperties.Instance exact = providers.get(providerName);
        if (exact != null) {
            return exact;
        }
        return providers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("NAB provider " + providerName + " is not configured"));
    }

    private void validateType(String providerName, String type) {
        if (!"nab".equalsIgnoreCase(StringUtils.trimToEmpty(type))) {
            throw new IllegalArgumentException("Provider " + providerName + " must define type=nab");
        }
    }

    private NabProperties.RqUid mergeRqUid(NabProperties.RqUid defaults, NabProperties.RqUid instance) {
        NabProperties.RqUid result = new NabProperties.RqUid();
        NabProperties.RqUid safeDefaults = defaults == null ? new NabProperties.RqUid() : defaults;
        NabProperties.RqUid safeInstance = instance == null ? new NabProperties.RqUid() : instance;
        result.setLength(first(safeInstance.getLength(), safeDefaults.getLength()));
        result.setType(first(safeInstance.getType(), safeDefaults.getType()));
        return result;
    }

    private NabProperties.CharacterNormalization mergeNormalization(
            NabProperties.CharacterNormalization defaults,
            NabProperties.CharacterNormalization instance
    ) {
        NabProperties.CharacterNormalization result = new NabProperties.CharacterNormalization();
        NabProperties.CharacterNormalization safeDefaults = defaults == null ? new NabProperties.CharacterNormalization() : defaults;
        NabProperties.CharacterNormalization safeInstance = instance == null ? new NabProperties.CharacterNormalization() : instance;
        result.setEnabled(first(safeInstance.getEnabled(), safeDefaults.getEnabled()));
        result.setReplacements(mergeMap(safeDefaults.getReplacements(), safeInstance.getReplacements()));
        return result;
    }

    private NabResolvedConfig.RateLimit resolvedRateLimit(
            ProviderRegistryProperties.RateLimit rateLimit,
            NabEndpointOverrides overrides,
            String providerName
    ) {
        ProviderRegistryProperties.RateLimit safe = rateLimit == null ? new ProviderRegistryProperties.RateLimit() : rateLimit;
        boolean enabled = Boolean.TRUE.equals(safe.getEnabled());
        String bucket = StringUtils.trimToNull(safe.getBucket());
        if (enabled && bucket == null) {
            throw new IllegalArgumentException("NAB provider " + providerName + " rate-limit.bucket is required when rate-limit is enabled");
        }
        String key = StringUtils.defaultIfBlank(safe.getKey(), "provider-operation");
        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }
        return new NabResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private NabResolvedConfig.RateLimit resolvedLegacyRateLimit(NabProperties.RateLimit rateLimit, NabEndpointOverrides overrides) {
        boolean enabled = Boolean.TRUE.equals(rateLimit.getEnabled());
        String bucket = StringUtils.defaultIfBlank(rateLimit.getBucket(), "nab-default");
        String key = StringUtils.defaultIfBlank(rateLimit.getKey(), "provider");
        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }
        return new NabResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private NabProperties.RateLimit mergeRateLimit(NabProperties.RateLimit defaults, NabProperties.RateLimit instance) {
        NabProperties.RateLimit result = new NabProperties.RateLimit();
        NabProperties.RateLimit safeDefaults = defaults == null ? new NabProperties.RateLimit() : defaults;
        NabProperties.RateLimit safeInstance = instance == null ? new NabProperties.RateLimit() : instance;
        result.setEnabled(first(safeInstance.getEnabled(), safeDefaults.getEnabled()));
        result.setBucket(first(safeInstance.getBucket(), safeDefaults.getBucket()));
        result.setKey(first(safeInstance.getKey(), safeDefaults.getKey()));
        return result;
    }

    private Map<String, List<NabFieldSpec>> resolveUnifiedHeaderFields(
            ProviderRegistryProperties.Provider instance,
            int rqUidLength,
            String configuredProtocol
    ) {
        Map<String, List<NabFieldSpec>> result = new LinkedHashMap<>();
        Map<String, List<ProviderRegistryProperties.Field>> configured = nonNullMap(instance.getHeaderFieldsByProtocol());
        configured.forEach((protocol, fields) -> result.put(protocol.toUpperCase(Locale.ROOT), toUnifiedFieldSpecs(fields, "header." + protocol)));
        List<ProviderRegistryProperties.Field> instanceHeaderFields = nonNullList(instance.getHeaderFields());
        if (!instanceHeaderFields.isEmpty()) {
            result.put(configuredProtocol, toUnifiedFieldSpecs(instanceHeaderFields, "header"));
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("NAB provider header-fields must be configured per provider instance");
        }
        return Map.copyOf(result);
    }

    private Map<String, List<NabFieldSpec>> resolveHeaderFields(
            NabProperties.Instance defaults,
            NabProperties.Instance instance,
            int rqUidLength,
            String configuredProtocol
    ) {
        Map<String, List<NabFieldSpec>> result = defaultHeaderFieldsByProtocol(rqUidLength);
        if (!nonNullList(defaults.getHeaderFields()).isEmpty()) {
            log.warn("NAB defaults.header-fields is ignored. Configure header-fields under each provider instance.");
        }
        if (!nonNullMap(defaults.getHeaderFieldsByProtocol()).isEmpty()) {
            log.warn("NAB defaults.header-fields-by-protocol is ignored. Configure header-fields-by-protocol under each provider instance.");
        }
        Map<String, List<NabProperties.Field>> configured = nonNullMap(instance.getHeaderFieldsByProtocol());
        configured.forEach((protocol, fields) -> result.put(protocol.toUpperCase(Locale.ROOT), toFieldSpecs(fields, "header." + protocol)));
        List<NabProperties.Field> instanceHeaderFields = nonNullList(instance.getHeaderFields());
        if (!instanceHeaderFields.isEmpty()) {
            if (configuredProtocol == null) {
                throw new IllegalArgumentException("NAB provider must set fixed protocol when header-fields is configured");
            }
            if (!configured.isEmpty()) {
                log.warn("NAB provider defines both header-fields and header-fields-by-protocol; header-fields has priority for protocol {}", configuredProtocol);
            }
            result.put(configuredProtocol, toFieldSpecs(instanceHeaderFields, "header"));
        }
        return Map.copyOf(result);
    }

    private Map<String, List<NabFieldSpec>> defaultHeaderFieldsByProtocol(int rqUidLength) {
        Map<String, List<NabFieldSpec>> result = new LinkedHashMap<>();
        result.put("ATPS", List.of(
                field("protocol", 4), field("command", 2), field("serviceCode", 2), field("dateTime", 14),
                field("userId", 10), field("password", 10), field("rqUid", rqUidLength)));
        result.put("ATPI", List.of(
                field("protocol", 4), field("clientAddress", 64), field("command", 2), field("serviceCode", 2),
                field("dateTime", 14), field("userId", 10), field("password", 10), field("rqUid", rqUidLength)));
        result.put("MIRS", result.get("ATPS"));
        return result;
    }

    private NabFieldSpec field(String name, int length) {
        return new NabFieldSpec(name, pointerForName(name), length, NabFieldType.STRING, true,
                "NONE", NabPadding.RIGHT_SPACE, NabOverflowPolicy.ERROR, true);
    }

    private List<NabFieldSpec> toFieldSpecs(List<NabProperties.Field> fields, String owner) {
        List<NabFieldSpec> result = new ArrayList<>();
        for (NabProperties.Field field : fields == null ? List.<NabProperties.Field>of() : fields) {
            result.add(toFieldSpec(field, owner));
        }
        return List.copyOf(result);
    }

    private NabFieldSpec toFieldSpec(NabProperties.Field field, String owner) {
        if (field == null) {
            throw new IllegalArgumentException(owner + " field spec must not be null");
        }
        return toFieldSpec(field.getName(), field.getPath(), field.getLength(), field.getType(), field.getRequired(),
                field.getConverter(), field.getPadding(), field.getOverflow(), field.getTrim(), owner);
    }

    private List<NabFieldSpec> toUnifiedFieldSpecs(List<ProviderRegistryProperties.Field> fields, String owner) {
        List<NabFieldSpec> result = new ArrayList<>();
        for (ProviderRegistryProperties.Field field : fields == null ? List.<ProviderRegistryProperties.Field>of() : fields) {
            result.add(toUnifiedFieldSpec(field, owner));
        }
        return List.copyOf(result);
    }

    private NabFieldSpec toUnifiedFieldSpec(ProviderRegistryProperties.Field field, String owner) {
        if (field == null) {
            throw new IllegalArgumentException(owner + " field spec must not be null");
        }
        return toFieldSpec(field.getName(), field.getPath(), field.getLength(), field.getType(), field.getRequired(),
                field.getConverter(), field.getPadding(), field.getOverflow(), field.getTrim(), owner);
    }

    private NabFieldSpec toFieldSpec(
            String fieldName,
            String path,
            Integer length,
            String type,
            Boolean required,
            String converter,
            String padding,
            String overflow,
            Boolean trim,
            String owner
    ) {
        String name = StringUtils.trimToNull(fieldName);
        if (name == null) {
            throw new IllegalArgumentException(owner + " field spec must define name");
        }
        if (length == null || length < 1) {
            throw new IllegalArgumentException(owner + " field " + name + " must define positive length");
        }
        return new NabFieldSpec(name, StringUtils.defaultIfBlank(path, pointerForName(name)), length,
                NabFieldType.from(type), Boolean.TRUE.equals(required), StringUtils.defaultIfBlank(converter, "NONE"),
                NabPadding.from(padding), NabOverflowPolicy.from(overflow), trim == null || trim);
    }

    private void logLegacyWarning() {
        if (legacyWarningLogged.compareAndSet(false, true)) {
            log.warn("Using deprecated scm.provider.nab configuration. Migrate NAB providers to scm.providers.<provider-code>.type=nab.");
        }
    }

    private static <T> T first(T value, T fallback) {
        return Objects.nonNull(value) ? value : fallback;
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

    private static List<String> nonBlankValues(List<String> values) {
        return values.stream().map(StringUtils::trimToNull).filter(Objects::nonNull).toList();
    }

    private static <K, V> Map<K, V> nonNullMap(Map<K, V> value) {
        return value == null ? Map.of() : value;
    }

    private static <K, V> Map<K, V> mergeMap(Map<K, V> defaults, Map<K, V> instance) {
        Map<K, V> result = new LinkedHashMap<>();
        result.putAll(nonNullMap(defaults));
        result.putAll(nonNullMap(instance));
        return result;
    }

    private static String pointerForName(String name) {
        return "/" + name.replace("~", "~0").replace("/", "~1");
    }
}
