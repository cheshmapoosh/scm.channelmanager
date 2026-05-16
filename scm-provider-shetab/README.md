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
              key: ${SCM_SHETAB_HPS_PIN_KEY}
              field: 52
              pan-field: 2
            expiry:
              field: 14
            cvv2:
              field: 48
              tag: P92
              length-digits: 3
              min-length: 3
              max-length: 4
            mac:
              key: ${SCM_SHETAB_HPS_MAC_KEY}
              field: 128
              verify-response: false
          rate-limit:
            enabled: true
            bucket: shetab-hps
```

Rate-limit bucket definitions are read from `scm-config` via `scm-rate-limit.config.definitions`.
HPS endpoint leases use `ResourceLeaseUtility` from `scm-cache-client` (`utilities.resource-lease=remote` for distributed mode).
Built-in packager classes are `Shetab7AsciiXAPackager` and `Shetab7BinaryXAPackager` (legacy `CardSystem...` class names are also accepted).

`fields.52` and `fields.128` are provider-owned.
Card security metadata should be passed in the `security` object:

- `security.expiryDate` (or `security.expirationDate`): `YYMM`, mapped to field `14` by provider.
- `security.cvv2`: numeric `3..4` digits, mapped to field `48` tag `P92` as `P92 + len(3 digits) + cvv2`.
- If caller already sends `P92` inside `fields.48`, provider removes it first and then rebuilds it from `security.cvv2`.
- Required flags are request-driven (not provider-instance driven): `pinRequired`, `expiryRequired`, `cvv2Required`, `macRequired`.
- If a value exists but its `*Required` flag is `false`, provider does not send that security field.

```json
{
  "mti": "1100",
  "fields": {
    "2": "5894631159226349",
    "3": "330000",
    "11": "261655",
    "37": "691199261655",
    "48": "DST0165894631240207217"
  },
  "security": {
    "expiryDate": "2907",
    "cvv2": "639",
    "pin": "1234",
    "pinRequired": true,
    "macRequired": true
  }
}
```

Human-readable request/response logs are written in `ShetabIsoChannelClient` via `SafeIsoLogFormatter`.
Sensitive values are masked/hidden in logs (for example `PAN`, `field 14`, `PIN block`, `MAC`, and `P92` CVV2 segment in `field 48`).

Run the real HPS card-inquiry test explicitly:

```bash
SCM_SHETAB_HPS_INTEGRATION=true \
SCM_SHETAB_HPS_ENDPOINTS=10.10.10.10:9000 \
SCM_SHETAB_HPS_PIN=1234 \
SCM_SHETAB_HPS_PIN_KEY=0123456789ABCDEF \
SCM_SHETAB_HPS_MAC_KEY=0123456789ABCDEF \
./gradlew :scm-provider-shetab:test --tests '*ShetabHpsCardInquiryIntegrationTest'
```
