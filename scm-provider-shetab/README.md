# scm-provider-shetab

`scm-provider-shetab` sends ISO8583 messages over TCP for `OperationType.PROVIDER` routes.

Example URI:

```text
shetab:request?provider=hps-shetab7
```

## Primary Configuration Model

Provider instances are configured under the unified registry:

```yaml
scm:
  providers:
    hps-shetab7:
      type: shetab
      enabled: true
      endpoint: 10.10.10.10:9000
      packager-class: Shetab7AsciiXAPackager
      connect-timeout-ms: 3000
      socket-timeout-ms: 1000
      response-timeout-ms: 6000
      send-timeout-ms: 1000
      reconnect-delay-ms: 1000
      queue-capacity: 1000
      rate-limit:
        enabled: true
        bucket: hps-shetab7
        key: provider-operation
      endpoint-lease:
        enabled: true
        ttl-ms: 30000
      message-customizers:
        - type: hps-shetab-outlet
          config:
            field: 42
            value: "123456789012345"
        - type: hps-shetab-terminal
          config:
            field: 41
            value: "12345678"
        - type: shetab-expiry
          config:
            field: 14
            source: security.expiryDate
        - type: shetab-cvv2
          config:
            field: 48
            tag: P92
            source: security.cvv2
            length-digits: 3
            min-length: 3
            max-length: 4
        - type: shetab-pin-block
          config:
            key: ${SCM_HPS_SHETAB7_PIN_KEY}
            field: 52
            pan-field: 2
            pin-source: security.pin
        - type: shetab-mac
          config:
            key: ${SCM_HPS_SHETAB7_MAC_KEY}
            field: 128
            verify-response: false
```

Rules:

- There is no defaults block in the primary model.
- Every provider instance must explicitly configure endpoint, packager, timeouts, rate-limit/lease if needed, and message customizers.
- No customizer is enabled by default.
- If `message-customizers` is missing or empty, no message mutation runs.
- Provider-level `security.pin`, `security.mac`, `security.expiry`, and `security.cvv2` are not part of the primary model.

## Request And Response

Input is a `Map` or JSON object:

```json
{
  "mti": "1100",
  "fields": {
    "2": "5894631150168490",
    "3": "330000",
    "11": "123456",
    "37": "123456789012"
  },
  "security": {
    "pin": "1234",
    "expiryDate": "2907",
    "cvv2": "639"
  }
}
```

Output is a `Map` with `mti` and `fields`.

## ProviderMessageCustomizer

`ProviderMessageCustomizerFactory` is the Spring bean extension point. YAML uses stable customizer `type` values, not bean names. The factory binds typed config and returns an immutable runtime `ProviderMessageCustomizer` instance.

Order convention:

- `100..999`: outlet, terminal, merchant, expiry, CVV2
- `5000`: authentication if a future ISO provider needs it
- `8000`: PIN block
- `10000`: MAC, only for ISO8583/Shetab
- `20000`: response enrichment

Shetab customizer types currently include:

- `hps-shetab-outlet`
- `hps-shetab-terminal`
- `shetab-expiry`
- `shetab-cvv2`
- `shetab-pin-block`
- `shetab-mac`

MAC must run after all request fields are finalized. PIN block must run before MAC. CVV2, expiry, outlet, terminal, and merchant enrichment must run before MAC.

REST provider has no MAC field. `shetab-mac` must never support REST transport.

## Observability And Security

Trace spans/events cover provider call and each configured customizer phase. Safe attributes include providerCode, providerType, serviceCode, operationCode, channelCode, transportType, customizerType, and phase.

Metrics include:

- `provider.request.duration`
- `provider.request.error`
- `provider.customizer.execution`
- `provider.customizer.error`

Logs include provider, operation, and trace/correlation context when available. Request/response debug bodies are masked.

Never log or trace PIN, PIN block, MAC, PAN, CVV2, expiry, password, token, or account number.

## Deprecated Legacy Compatibility

`scm.provider.shetab.defaults/providers` and provider-level `security.*` are deprecated. They may be resolved for compatibility, but new provider instances must use `scm.providers.<provider-code>.type=shetab` and explicit `message-customizers`.

## Distributed Deployment

- Distributed rate limit uses `RateLimiterUtility` from `scm-cache-client`.
- Endpoint lease uses `ResourceLeaseUtility` from `scm-cache-client`.
- Without those utilities, rate limit falls back to noop and endpoint lease falls back to local behavior with WARN logs.
