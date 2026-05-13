# scm-provider-shetab

Shetab/HPS provider module for ISO8583 over TCP.

The operation route should use `OperationType.PROVIDER` and point to a provider URI:

```text
shetab:request?provider=poya
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
      pod-id: ${HOSTNAME:${spring.application.name:scm-web}}
      defaults:
        connect-timeout-ms: 3000
        socket-timeout-ms: 1000
        response-timeout-ms: 6000
        send-timeout-ms: 1000
        reconnect-delay-ms: 1000
        queue-capacity: 1000
        channel-type: ASCII
        length-digits: 4
        rate-limit:
          enabled: false
          bucket: shetab-default
          key: provider
        port-lease:
          enabled: true
          ttl-ms: 30000
      providers:
        poya:
          host: 10.10.10.10
          port: 9000
          local-address: 0.0.0.0
          local-ports: [41001, 41002, 41003]
          rate-limit:
            enabled: true
            bucket: shetab-poya
```

Rate-limit bucket definitions are read from `scm-config` via `scm-rate-limit.config.definitions`.
Local-port leases use `ResourceLeaseUtility` from `scm-cache-client` (`utilities.resource-lease=remote` for distributed mode).
