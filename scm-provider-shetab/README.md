# scm-provider-shetab

Shetab/HPS provider module for ISO8583 over TCP.

The operation route should use `OperationType.PROVIDER` and point to a provider URI:

```text
shetab:request?provider=hps
```

The provider receives a `Map` body in this shape:

```json
{
  "mti": "1100",
  "fields": {
    "2": "5894631150168490",
    "3": "330000",
    "11": "123456",
    "37": "123456789012"
  }
}
```

Provider instance configuration:

```yaml
scm:
  provider:
    shetab:
      enabled: true
      defaults:
        connect-timeout-ms: 3000
        socket-timeout-ms: 1000
        response-timeout-ms: 6000
        send-timeout-ms: 1000
        reconnect-delay-ms: 1000
        same-endpoint-reconnect-attempts: 3
        queue-capacity: 1000
        rate-limit:
          enabled: false
          bucket: shetab-default
          key: provider
        endpoint-lease:
          enabled: true
          ttl-ms: 30000
      providers:
        hps:
          endpoints: [10.10.10.10:9000, 10.10.10.11:9000, 10.10.10.12:9000]
          packager-class: Shetab7AsciiXAPackager
          security:
            pin:
              enabled: true
              key: ${SCM_SHETAB_HPS_PIN_KEY}
            mac:
              enabled: true
              key: ${SCM_SHETAB_HPS_MAC_KEY}
          rate-limit:
            enabled: true
            bucket: shetab-hps
```

Rate-limit bucket definitions are read from `scm-config` via `scm-rate-limit.config.definitions`.
HPS endpoint leases use `ResourceLeaseUtility` from `scm-cache-client` (`utilities.resource-lease=remote` for distributed mode).
Built-in packager classes are `Shetab7AsciiXAPackager` and `Shetab7BinaryXAPackager` (legacy `CardSystem...` class names are also accepted).

`fields.52` and `fields.128` are provider-owned. Operation transformers should pass normal ISO fields plus optional security metadata:

```json
{
  "mti": "1100",
  "fields": {
    "2": "5894631159226349",
    "3": "330000",
    "11": "261655",
    "37": "691199261655"
  },
  "security": {
    "pin": "1234",
    "pinRequired": true,
    "macRequired": true
  }
}
```

Run the real HPS card-inquiry test explicitly:

```bash
SCM_SHETAB_HPS_INTEGRATION=true \
SCM_SHETAB_HPS_ENDPOINTS=10.10.10.10:9000 \
SCM_SHETAB_HPS_PIN=1234 \
SCM_SHETAB_HPS_PIN_KEY=0123456789ABCDEF \
SCM_SHETAB_HPS_MAC_KEY=0123456789ABCDEF \
./gradlew :scm-provider-shetab:test --tests '*ShetabHpsCardInquiryIntegrationTest'
```
