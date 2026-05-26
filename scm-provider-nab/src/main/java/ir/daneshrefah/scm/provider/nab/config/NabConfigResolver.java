package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldType;
import ir.daneshrefah.scm.provider.nab.domain.NabOverflowPolicy;
import ir.daneshrefah.scm.provider.nab.domain.NabPadding;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class NabConfigResolver {
    private final NabProperties properties;

    public NabResolvedConfig resolve(String provider, NabEndpointOverrides overrides) {
        String providerName = normalizeProviderName(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("NAB provider is required");
        }

        NabProperties.Instance instance = findProvider(providerName);
        NabProperties.Instance defaults = properties.getDefaults();
        List<String> endpoints = nonNullList(instance.getEndpoints()).isEmpty()
                ? nonNullList(defaults.getEndpoints())
                : nonNullList(instance.getEndpoints());
        if (endpoints.isEmpty()) {
            throw new IllegalArgumentException("NAB provider " + providerName + " must define at least one endpoint (host:port)");
        }

        NabProperties.RqUid rqUid = mergeRqUid(defaults.getRqUid(), instance.getRqUid());
        NabProperties.CharacterNormalization normalization = mergeNormalization(
                defaults.getCharacterNormalization(),
                instance.getCharacterNormalization()
        );
        NabProperties.ConnectionPool connectionPool = mergeConnectionPool(
                defaults.getConnectionPool(),
                instance.getConnectionPool()
        );
        String charset = overrides != null && StringUtils.isNotBlank(overrides.charset())
                ? overrides.charset()
                : first(instance.getCharset(), defaults.getCharset());
        int responseTimeoutMs = overrides != null && overrides.timeoutMs() != null
                ? overrides.timeoutMs()
                : value(first(instance.getResponseTimeoutMs(), defaults.getResponseTimeoutMs()), 6000);
        int rqUidLength = value(rqUid.getLength(), 16);
        int poolMaxTotal = positive(first(connectionPool.getMaxTotal(), connectionPool.getMaxSize()), 16);
        int poolMaxIdle = Math.min(poolMaxTotal, positive(connectionPool.getMaxIdle(), poolMaxTotal));
        int poolMinIdle = Math.min(poolMaxIdle, nonNegative(connectionPool.getMinIdle(), 0));
        int poolMaxWaitMs = positive(first(connectionPool.getMaxWaitMs(), connectionPool.getBorrowTimeoutMs()), 1000);
        long minEvictableIdleTimeMs = positive(
                first(connectionPool.getMinEvictableIdleTimeMs(), connectionPool.getMaxIdleTimeMs()),
                60_000L
        );

        return new NabResolvedConfig(
                providerName,
                endpoints,
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
                resolveHeaderFields(defaults, instance, rqUidLength),
                new NabResolvedConfig.RqUid(
                        rqUidLength,
                        StringUtils.defaultIfBlank(rqUid.getType(), "NUMERIC")
                ),
                new NabResolvedConfig.CharacterNormalization(
                        Boolean.TRUE.equals(normalization.getEnabled()),
                        Map.copyOf(nonNullMap(normalization.getReplacements()))
                ),
                new NabResolvedConfig.ConnectionPool(
                        booleanValue(connectionPool.getEnabled(), true),
                        poolMaxTotal,
                        poolMinIdle,
                        poolMaxIdle,
                        poolMaxWaitMs,
                        minEvictableIdleTimeMs,
                        positive(connectionPool.getSoftMinEvictableIdleTimeMs(), minEvictableIdleTimeMs),
                        positive(connectionPool.getTimeBetweenEvictionRunsMs(), 30_000L),
                        positive(connectionPool.getMaxLifeTimeMs(), 300_000L),
                        booleanValue(first(connectionPool.getTestOnBorrow(), connectionPool.getValidationEnabled()), true),
                        booleanValue(connectionPool.getTestOnReturn(), false),
                        booleanValue(connectionPool.getTestWhileIdle(), true),
                        booleanValue(connectionPool.getBlockWhenExhausted(), true),
                        booleanValue(connectionPool.getLifo(), true),
                        booleanValue(connectionPool.getPrefill(), false)
                ),
                booleanValue(first(instance.getWireLogEnabled(), defaults.getWireLogEnabled()), true)
        );
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

    private NabProperties.Instance findProvider(String providerName) {
        Map<String, NabProperties.Instance> providers = properties.getProviders();
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

    private NabProperties.ConnectionPool mergeConnectionPool(
            NabProperties.ConnectionPool defaults,
            NabProperties.ConnectionPool instance
    ) {
        NabProperties.ConnectionPool result = new NabProperties.ConnectionPool();
        NabProperties.ConnectionPool safeDefaults = defaults == null ? new NabProperties.ConnectionPool() : defaults;
        NabProperties.ConnectionPool safeInstance = instance == null ? new NabProperties.ConnectionPool() : instance;
        result.setEnabled(first(safeInstance.getEnabled(), safeDefaults.getEnabled()));
        result.setMaxTotal(first(safeInstance.getMaxTotal(), safeDefaults.getMaxTotal()));
        result.setMaxSize(first(safeInstance.getMaxSize(), safeDefaults.getMaxSize()));
        result.setMinIdle(first(safeInstance.getMinIdle(), safeDefaults.getMinIdle()));
        result.setMaxIdle(first(safeInstance.getMaxIdle(), safeDefaults.getMaxIdle()));
        result.setMaxWaitMs(first(safeInstance.getMaxWaitMs(), safeDefaults.getMaxWaitMs()));
        result.setBorrowTimeoutMs(first(safeInstance.getBorrowTimeoutMs(), safeDefaults.getBorrowTimeoutMs()));
        result.setMaxIdleTimeMs(first(safeInstance.getMaxIdleTimeMs(), safeDefaults.getMaxIdleTimeMs()));
        result.setMinEvictableIdleTimeMs(first(safeInstance.getMinEvictableIdleTimeMs(), safeDefaults.getMinEvictableIdleTimeMs()));
        result.setSoftMinEvictableIdleTimeMs(first(safeInstance.getSoftMinEvictableIdleTimeMs(), safeDefaults.getSoftMinEvictableIdleTimeMs()));
        result.setTimeBetweenEvictionRunsMs(first(safeInstance.getTimeBetweenEvictionRunsMs(), safeDefaults.getTimeBetweenEvictionRunsMs()));
        result.setMaxLifeTimeMs(first(safeInstance.getMaxLifeTimeMs(), safeDefaults.getMaxLifeTimeMs()));
        result.setValidationEnabled(first(safeInstance.getValidationEnabled(), safeDefaults.getValidationEnabled()));
        result.setTestOnBorrow(first(safeInstance.getTestOnBorrow(), safeDefaults.getTestOnBorrow()));
        result.setTestOnReturn(first(safeInstance.getTestOnReturn(), safeDefaults.getTestOnReturn()));
        result.setTestWhileIdle(first(safeInstance.getTestWhileIdle(), safeDefaults.getTestWhileIdle()));
        result.setBlockWhenExhausted(first(safeInstance.getBlockWhenExhausted(), safeDefaults.getBlockWhenExhausted()));
        result.setLifo(first(safeInstance.getLifo(), safeDefaults.getLifo()));
        result.setPrefill(first(safeInstance.getPrefill(), safeDefaults.getPrefill()));
        return result;
    }

    private Map<String, List<NabFieldSpec>> resolveHeaderFields(
            NabProperties.Instance defaults,
            NabProperties.Instance instance,
            int rqUidLength
    ) {
        Map<String, List<NabFieldSpec>> result = defaultHeaderFieldsByProtocol(rqUidLength);
        Map<String, List<NabProperties.Field>> configured = mergeMap(
                defaults.getHeaderFieldsByProtocol(),
                instance.getHeaderFieldsByProtocol()
        );
        configured.forEach((protocol, fields) ->
                result.put(protocol.toUpperCase(Locale.ROOT), toFieldSpecs(fields, "header." + protocol)));
        return Map.copyOf(result);
    }

    private Map<String, List<NabFieldSpec>> defaultHeaderFieldsByProtocol(int rqUidLength) {
        Map<String, List<NabFieldSpec>> result = new LinkedHashMap<>();
        result.put("ATPS", List.of(
                field("nabProtocol", 4),
                field("command", 2),
                field("serviceCode", 2),
                field("dateTime", 14),
                field("userId", 10),
                field("password", 10),
                field("rqUid", rqUidLength)
        ));
        result.put("ATPI", List.of(
                field("nabProtocol", 4),
                field("clientAddress", 64),
                field("command", 2),
                field("serviceCode", 2),
                field("dateTime", 14),
                field("userId", 10),
                field("password", 10),
                field("rqUid", rqUidLength)
        ));
        result.put("MIRS", result.get("ATPS"));
        return result;
    }

    private NabFieldSpec field(String name, int length) {
        return new NabFieldSpec(
                name,
                pointerForName(name),
                length,
                NabFieldType.STRING,
                true,
                "NONE",
                NabPadding.RIGHT_SPACE,
                NabOverflowPolicy.ERROR,
                true
        );
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
        String name = StringUtils.trimToNull(field.getName());
        if (name == null) {
            throw new IllegalArgumentException(owner + " field spec must define name");
        }
        if (field.getLength() == null || field.getLength() < 1) {
            throw new IllegalArgumentException(owner + " field " + name + " must define positive length");
        }
        return new NabFieldSpec(
                name,
                StringUtils.defaultIfBlank(field.getPath(), pointerForName(name)),
                field.getLength(),
                NabFieldType.from(field.getType()),
                Boolean.TRUE.equals(field.getRequired()),
                StringUtils.defaultIfBlank(field.getConverter(), "NONE"),
                NabPadding.from(field.getPadding()),
                NabOverflowPolicy.from(field.getOverflow()),
                field.getTrim() == null || field.getTrim()
        );
    }

    private static <T> T first(T value, T fallback) {
        return Objects.nonNull(value) ? value : fallback;
    }

    private static int value(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private static int positive(Integer value, int fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private static long positive(Long value, long fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private static int nonNegative(Integer value, int fallback) {
        return value != null && value >= 0 ? value : fallback;
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
