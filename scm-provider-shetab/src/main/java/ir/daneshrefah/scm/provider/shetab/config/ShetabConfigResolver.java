package ir.daneshrefah.scm.provider.shetab.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ShetabConfigResolver {
    private final ShetabProperties properties;

    public ShetabResolvedConfig resolve(String provider, ShetabEndpointOverrides overrides) {
        String providerName = StringUtils.trimToNull(provider);
        if (providerName == null) {
            throw new IllegalArgumentException("Shetab provider is required");
        }

        ShetabProperties.Instance instance = findProvider(providerName);
        ShetabProperties.Instance defaults = properties.getDefaults();
        String host = first(instance.getHost(), defaults.getHost());
        Integer port = first(instance.getPort(), defaults.getPort());
        if (StringUtils.isBlank(host) || port == null) {
            throw new IllegalArgumentException("Shetab provider " + providerName + " must define host and port");
        }

        ShetabProperties.RateLimit rateLimit = mergeRateLimit(defaults.getRateLimit(), instance.getRateLimit());
        ShetabProperties.PortLease portLease = mergePortLease(defaults.getPortLease(), instance.getPortLease());
        int responseTimeout = value(first(instance.getResponseTimeoutMs(), defaults.getResponseTimeoutMs()), 6000);

        return new ShetabResolvedConfig(
                providerName,
                host,
                port,
                first(instance.getLocalAddress(), defaults.getLocalAddress()),
                nonNullList(instance.getLocalPorts()).isEmpty() ? nonNullList(defaults.getLocalPorts()) : nonNullList(instance.getLocalPorts()),
                value(first(instance.getChannelType(), defaults.getChannelType()), "ASCII"),
                value(first(instance.getLengthDigits(), defaults.getLengthDigits()), 4),
                first(instance.getPackagerClass(), defaults.getPackagerClass()),
                first(instance.getPackagerXml(), defaults.getPackagerXml()),
                value(first(instance.getConnectTimeoutMs(), defaults.getConnectTimeoutMs()), 3000),
                value(first(instance.getSocketTimeoutMs(), defaults.getSocketTimeoutMs()), 1000),
                overrides != null && overrides.timeoutMs() != null ? overrides.timeoutMs() : responseTimeout,
                value(first(instance.getSendTimeoutMs(), defaults.getSendTimeoutMs()), 1000),
                value(first(instance.getReconnectDelayMs(), defaults.getReconnectDelayMs()), 1000),
                value(first(instance.getQueueCapacity(), defaults.getQueueCapacity()), 1000),
                resolvedRateLimit(rateLimit, overrides),
                new ShetabResolvedConfig.PortLease(
                        Boolean.TRUE.equals(portLease.getEnabled()),
                        value(portLease.getTtlMs(), 30000L)
                )
        );
    }

    private ShetabProperties.Instance findProvider(String providerName) {
        Map<String, ShetabProperties.Instance> providers = properties.getProviders();
        ShetabProperties.Instance exact = providers.get(providerName);
        if (exact != null) {
            return exact;
        }
        return providers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).equals(providerName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Shetab provider " + providerName + " is not configured"));
    }

    private ShetabResolvedConfig.RateLimit resolvedRateLimit(ShetabProperties.RateLimit rateLimit, ShetabEndpointOverrides overrides) {
        boolean enabled = Boolean.TRUE.equals(rateLimit.getEnabled());
        String bucket = value(rateLimit.getBucket(), "shetab-default");
        String key = value(rateLimit.getKey(), "provider");

        if (overrides != null) {
            enabled = overrides.rateLimitEnabled() != null ? overrides.rateLimitEnabled() : enabled;
            bucket = StringUtils.defaultIfBlank(overrides.rateLimitBucket(), bucket);
            key = StringUtils.defaultIfBlank(overrides.rateLimitKey(), key);
        }

        return new ShetabResolvedConfig.RateLimit(enabled, bucket, key);
    }

    private ShetabProperties.RateLimit mergeRateLimit(ShetabProperties.RateLimit defaults, ShetabProperties.RateLimit instance) {
        ShetabProperties.RateLimit result = new ShetabProperties.RateLimit();
        result.setEnabled(first(instance.getEnabled(), defaults.getEnabled()));
        result.setBucket(first(instance.getBucket(), defaults.getBucket()));
        result.setKey(first(instance.getKey(), defaults.getKey()));
        return result;
    }

    private ShetabProperties.PortLease mergePortLease(ShetabProperties.PortLease defaults, ShetabProperties.PortLease instance) {
        ShetabProperties.PortLease result = new ShetabProperties.PortLease();
        result.setEnabled(first(instance.getEnabled(), defaults.getEnabled()));
        result.setTtlMs(first(instance.getTtlMs(), defaults.getTtlMs()));
        return result;
    }

    private static <T> T first(T value, T fallback) {
        return Objects.nonNull(value) ? value : fallback;
    }

    private static int value(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private static long value(Long value, long fallback) {
        return value != null ? value : fallback;
    }

    private static String value(String value, String fallback) {
        return StringUtils.defaultIfBlank(value, fallback);
    }

    private static List<Integer> nonNullList(List<Integer> value) {
        return value == null ? List.of() : List.copyOf(value);
    }
}
