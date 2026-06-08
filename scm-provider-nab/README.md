# scm-provider-nab

`scm-provider-nab` sends fixed-length NAB TCP messages. It allows command request/response structures to be supplied as JSON field specs instead of Java mappers.

Example URI:

```text
scm-nab:nab-atps
```

Operation provider URIs follow `scm-<provider-type>:<provider-code>`, for example `scm-nab:nab-atps`.

## Primary Configuration Model

Provider instances are configured under the unified registry:

```yaml
scm:
  providers:
    nab-atps:
      type: nab
      enabled: true
      protocol: ATPS
      endpoint: 10.10.10.10:3080
      connect-timeout-ms: 3000
      socket-timeout-ms: 1000
      response-timeout-ms: 6000
      ack-length-bytes: 5
      charset: windows-1252
      user-id: "999998"
      password: ${NAB_ATPS_PASSWORD}
      rq-uid:
        length: 16
        type: NUMERIC
      header-fields:
        - { name: "protocol", length: 4, required: true }
        - { name: "command", length: 2, required: true }
        - { name: "serviceCode", length: 2, required: true }
        - { name: "dateTime", length: 14, required: true }
        - { name: "userId", length: 10, required: true }
        - { name: "password", length: 10, required: true }
        - { name: "rqUid", length: 16, required: true }
      rate-limit:
        enabled: true
        bucket: nab-atps
        key: provider-operation
```

Rules:

- There is no defaults block in the primary model.
- Every NAB provider instance must explicitly configure protocol, endpoint, timeouts, credentials, `rq-uid`, `header-fields`, and rate-limit if needed.
- `header-fields` are provider-instance specific and must not be documented or configured under defaults.
- Missing required fields fail fast.
- `type: nab` makes this module own and validate the instance.
- If `message-customizers` is missing or empty, no customizer pipeline runs.

## Request Shape

Input body must be a `JsonNode` or JSON object convertible to `JsonNode`:

```json
{
  "command": {
    "code": "27",
    "protocol": "ATPI"
  },
  "header": {
    "terminalType": "ATM",
    "channelCode": "MOBILE",
    "clientAddress": "10.1.1.10"
  },
  "data": {
    "customerId": "123456"
  },
  "request": {
    "fields": [
      { "name": "customerId", "length": 12, "required": true }
    ]
  },
  "response": {
    "fields": [
      { "name": "accountNo", "length": 18 }
    ]
  }
}
```

## Header Fields

The provider builds the NAB header from configured `header-fields`. Field order is the array order.

Common field spec:

```json
{
  "name": "amount",
  "length": 18,
  "path": "/transfer/amount",
  "type": "NUMBER",
  "required": true,
  "converter": "TRIM",
  "padding": "LEFT_ZERO",
  "overflow": "ERROR",
  "trim": true
}
```

Rules:

- `name` is required.
- `length` is required.
- `path` defaults to `/<name>`.
- `type` defaults to `STRING`.
- `padding` defaults to `RIGHT_SPACE`.
- `overflow` defaults to `ERROR`.

## Rate Limit

Use provider-instance `rate-limit`:

- `rate-limit.enabled`
- `rate-limit.bucket`
- `rate-limit.key`: `provider`, `operation`, or `provider-operation`

Runtime overrides are still available through NAB provider headers and endpoint URI params.

## ProviderMessageCustomizer

NAB uses the common factory-based ProviderMessageCustomizer architecture. Factories are Spring beans, YAML uses stable `message-customizers[].type` values, and runtime customizers are immutable instances created per resolved provider.

`scm-common` provides Spring Boot auto-configuration for the common provider registry, customizer factory registry, and pipeline factory; provider modules do not need to component-scan common provider infrastructure manually.

The NAB resolver builds the provider instance pipeline once. No customizer is enabled by default.

## Observability And Security

Metrics follow the provider metric style and include provider request counters/latency and rate-limit counters. Logs include provider and operation context. Do not log password, account number, PAN, token, PIN, or other sensitive values.
