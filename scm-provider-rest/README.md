# scm-provider-rest

`scm-provider-rest` is the reusable REST provider for `OperationType.PROVIDER` routes.

Example URI:

```text
scm-rest:hps-rest
```

Operation provider URIs follow `<scheme>:<providerCode>`, for example `scm-rest:hps-rest`.
Do not use `rest:` for SCM REST provider routes; Apache Camel reserves `rest:` for its built-in REST DSL endpoint syntax.

## Primary Configuration Model

Provider instances are configured under the unified registry:

```yaml
scm:
  providers:
    hps-rest:
      scheme: scm-rest
      enabled: true
      base-url: https://hps-rest.example.ir
      connect-timeout-ms: 3000
      response-timeout-ms: 6000
      virtual-threads-enabled: true
      insecure-ssl: false
      headers:
        Accept: application/json
        Content-Type: application/json
      rate-limit:
        enabled: true
        bucket: hps-rest
        key: provider-operation
      message-customizers:
        - type: rest-auth-url
          config:
            url: https://hps-rest.example.ir/oauth/token
            method: POST
            request:
              headers:
                Content-Type: application/x-www-form-urlencoded
              form:
                grant_type: client_credentials
                client_id: ${HPS_REST_CLIENT_ID}
                client_secret: ${HPS_REST_CLIENT_SECRET}
            response:
              token-field: access_token
              expires-in-field: expires_in
              token-type-field: token_type
              default-token-type: Bearer
            cache:
              name: rest_provider_token_cache
              key-prefix: provider-token
              auth-profile: default
              credential-key: hps-rest
              refresh-skew: 60s
              ttl-skew: 5s
            lock:
              key-prefix: provider-token-refresh-lock
              wait-timeout: 3s
              retry-delay: 100ms
            apply:
              location: header
              name: Authorization
              format: "{tokenType} {accessToken}"
        - type: hps-rest-outlet
          config:
            location: body
            name: outlet
            value: "123456789012345"
```

Rules:

- There is no defaults block in the primary model.
- Every provider instance must be explicitly configured.
- `providerCode` is the key under `scm.providers`.
- Provider config uses `scm.providers.<providerCode>.scheme = scm-rest`, not provider `type`.
- `scheme: scm-rest` makes this module own and validate the instance.
- Customizer `type` values identify customizer factories and are separate from provider `scheme`.
- Missing required fields fail fast.
- If `message-customizers` is missing or empty, no customizer runs.
- No customizer is enabled by default.
- Provider-level auth/token/cache/lock/apply settings are not part of the primary model.
- REST authentication must be configured through `message-customizers`.
- `base-url` (and its legacy `endpoint` alias) is optional only when every
  effective Operation for that provider uses an absolute HTTP(S)
  `Operation.path`.

## Operation Destination Contract

Every effective SCM REST Operation owns exactly one immutable HTTP(S) target.
It is resolved during Operation-layer route construction using this matrix:

| Provider `base-url` | `Operation.path` | Startup result |
| --- | --- | --- |
| Present | Safe relative path | Join base URL and path |
| Absent | Absolute HTTP(S) URL | Use `Operation.path` directly |
| Present | Absolute URL | Fail: conflicting configuration |
| Absent | Relative path | Fail: no base URL |
| Any | Missing or blank | Fail: path is required |
| Any | Invalid final URI | Fail: invalid configuration |

Blank `base-url` is treated as absent. The legacy provider `endpoint` property
continues to be an alias for `base-url`.

### Base URL plus relative path

The usual configuration keeps provider authority in configuration and the
endpoint-specific suffix in the Operation:

```yaml
scm:
  providers:
    hps-rest:
      scheme: scm-rest
      enabled: true
      base-url: https://provider.example/root/
```

```text
OperationProvider:
  name   = HPS_REST
  uri    = scm-rest:hps-rest
  active = true

Operation:
  name     = CARD_INQUIRY
  type     = PROVIDER
  provider = HPS_REST
  path     = /v1/card/inquiry
  active   = true
```

The resolved target is:

```text
https://provider.example/root/v1/card/inquiry
```

The relative path may have a leading slash. Joining preserves an existing base
path and inserts exactly one slash at the boundary.

### No base URL plus absolute Operation path

A provider may omit both `base-url` and its `endpoint` alias when its effective
Operations each contain their complete destination:

```yaml
scm:
  providers:
    hps-rest:
      scheme: scm-rest
      enabled: true
```

```text
Operation:
  name     = CARD_INQUIRY
  type     = PROVIDER
  provider = HPS_REST
  path     = https://provider.example/root/v1/card/inquiry
  active   = true
```

This resolves exactly to the absolute `Operation.path`; no base URL is
prepended, appended, or inferred.

### Validation and conflicting configuration

Base URLs and absolute Operation paths must use HTTP or HTTPS, contain a valid
host and explicit port when one is configured, and contain no user information,
query, or fragment. Paths reject backslashes, duplicate separators, literal or
encoded traversal segments, and encoded slash or backslash separators.

These configurations are invalid:

```text
base-url = https://provider.example/root
path     = https://other.example/v1/card/inquiry

base-url = <absent>
path     = /v1/card/inquiry

base-url = https://provider.example/root
path     = <blank>
```

The provider runtime lifecycle resolves every effective target before request
serving, stores it by stable Operation name, and publishes an immutable registry
when registration completes. Invalid configuration emits one structured ERROR
event named `rest_provider_operation_target_validation_failed` with bounded
service, Operation, provider, path-type, and reason fields, followed by startup
failure. Raw malformed URLs, credentials, query values, bodies, and headers are
not included in that event.

For Operation invocations, request envelope/header `url` and `path` values are
not destination inputs and cannot override the startup-resolved scheme, host,
port, or path. Request and customizer query parameters may be appended to the
fixed target but cannot replace those components. Runtime processing performs
no database lookup, base URL/path parsing, joining, or replacement target
construction.

A direct `scm-rest:` component call with no `Message.OPERATION` retains the
legacy envelope/header URL and path behavior. With no provider base URL, such a
direct call must provide an absolute HTTP(S) request `url`.

## Request And Response

The `url` and `path` fields shown below apply to direct component calls. When
the call originates from an SCM Operation, `Operation.path` is authoritative
and these two fields are ignored.

Request body can be a direct payload or an envelope:

```json
{
  "method": "POST",
  "path": "/api/v1/card/inquiry",
  "query": {
    "channel": "mobile"
  },
  "headers": {
    "X-Correlation-Id": "abc-123"
  },
  "body": {
    "pan": "5894631150168490"
  }
}
```

Response body:

```json
{
  "status": 200,
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "resultCode": "00"
  }
}
```

## ProviderMessageCustomizer

The runtime contract is Spring-independent:

```java
public interface ProviderMessageCustomizer {
    int order();

    default void beforeSend(ProviderExchange exchange) {
    }

    default void afterReceive(ProviderExchange exchange) {
    }
}
```

The Spring extension point is the factory:

```java
public interface ProviderMessageCustomizerFactory<C> {
    String type();
    Class<C> configType();
    int defaultOrder();
    ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, C config);
}
```

`scm-common` provides Spring Boot auto-configuration for `ProviderRegistryProperties`, `ProviderMessageCustomizerFactoryRegistry`, and `ProviderMessageCustomizerPipelineFactory`; provider modules do not need to component-scan common provider infrastructure manually.

How it works:

1. The provider instance lists `message-customizers` by stable `type`.
2. The pipeline builder resolves a `ProviderMessageCustomizerFactory` by `type`.
3. `config` is bound to the factory `configType()`.
4. The factory returns an immutable runtime `ProviderMessageCustomizer` instance.
5. `order` from YAML overrides the factory default order when present.
6. The provider resolver builds the pipeline once for the resolved provider instance.
7. The producer executes only that configured provider instance pipeline.

Do not put Spring bean names or factory class names in YAML.

Recommended order ranges:

- `100..999`: field enrichment such as outlet, terminal, merchant, CVV2, expiry
- `5000`: authentication
- `8000`: PIN block
- `10000`: MAC only for ISO8583/Shetab, never REST
- `20000`: response enrichment

Example factory-created customizer:

```java
@Component
public class OutletProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<OutletConfig> {

    @Override
    public String type() {
        return "hps-rest-outlet";
    }

    @Override
    public Class<OutletConfig> configType() {
        return OutletConfig.class;
    }

    @Override
    public int defaultOrder() {
        return 100;
    }

    @Override
    public ProviderMessageCustomizer create(
            ProviderMessageCustomizerFactoryContext context,
            OutletConfig config
    ) {
        return new OutletProviderMessageCustomizer(config.name(), config.value());
    }
}

public record OutletProviderMessageCustomizer(String name, String value)
        implements ProviderMessageCustomizer {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public void beforeSend(ProviderExchange exchange) {
        exchange.request().putField(name, value);
    }
}
```

## REST Auth URL

REST auth URL is configured only as customizer type `rest-auth-url`. Do not configure auth URL under provider-level `auth`, `token`, `cache`, `lock`, or `apply`.

The customizer:

- Calls `ProviderAuthTokenProvider` / `RestProviderTokenManager` for token resolution.
- Applies the token according to `apply.location`, `apply.name`, and `apply.format`.
- Supports header, body field, and query parameter application.
- Does not hardcode `Authorization`.

Token cache and lock algorithm:

1. Build cache key: `provider-token:<providerCode>:<authProfile>:<channelCode>:<credentialKey>`.
2. Check centralized token cache first.
3. If a valid token exists considering `refresh-skew`, return it.
4. Acquire distributed lock: `provider-token-refresh-lock:<providerCode>:<authProfile>:<channelCode>:<credentialKey>`.
5. Check centralized cache again after lock acquisition.
6. Only the lock owner calls the auth URL.
7. Store token in centralized cache with TTL based on expiry minus `ttl-skew`.
8. Release the lock through `LockUtility`.
9. If lock cannot be acquired, poll centralized cache until `lock.wait-timeout` using `lock.retry-delay`.
10. If no token appears, throw provider auth fault.

No local-only fallback is used when centralized cache is configured.

## REST Static Auth

Static REST authentication is configured only as customizer type `rest-static-auth`:

```yaml
message-customizers:
  - type: rest-static-auth
    config:
      type: BASIC
      header-name: Authorization
      prefix: Basic
      username: ${REST_USERNAME}
      password: ${REST_PASSWORD}
      basic-base64: true
```

Supported types are `BASIC`, `BEARER`, `JWT`, and `API_KEY`. The customizer supports custom header names and is not enabled unless it is listed in `message-customizers`.

## REST Has No MAC

REST provider has no MAC field. Do not configure or implement MAC for REST. A Shetab/ISO8583 MAC customizer must reject `scm-rest` providers.

## Observability And Security

Trace spans/events cover:

- provider call
- customizer execution
- auth token resolution
- centralized cache lookup
- distributed lock acquire/wait
- auth URL call
- token cache put

Logs include provider, service, operation, channel, correlation/trace IDs when available. Debug logs include configured customizer type/order and auth cache/lock events.

Metrics include:

- `provider.request.duration`
- `provider.request.error`
- `provider.customizer.execution`
- `provider.customizer.error`
- `provider.auth.cache.hit`
- `provider.auth.cache.miss`
- `provider.auth.lock.acquired`
- `provider.auth.lock.timeout`
- `provider.auth.token.refresh`
- `provider.auth.token.refresh.error`
- `provider.auth.request.duration`

Metric tags are low cardinality: providerCode, scheme, serviceCode, operationCode, channelCode, customizerType, outcome.

Never log or trace tokens, username/password, client secret, PIN, PIN block, MAC, PAN, account number, CVV2, or raw sensitive body values.

## Troubleshooting

- `MISSING_OPERATION_PATH`: configure either a relative path with provider
  `base-url` or an absolute HTTP(S) `Operation.path` without `base-url`.
- `BASE_URL_AND_ABSOLUTE_PATH_CONFLICT`: remove `base-url`/`endpoint`, or change
  `Operation.path` to a relative path.
- `RELATIVE_PATH_WITHOUT_BASE_URL`: configure provider `base-url`, or replace the
  path with an absolute HTTP(S) URL.
- `INVALID_BASE_URL`, `INVALID_ABSOLUTE_OPERATION_URL`, or
  `INVALID_RELATIVE_OPERATION_PATH`: correct the indicated URI form according to
  the validation rules above. The startup ERROR event identifies the affected
  service, Operation, and provider without printing the raw URI.
- Unexpected REST destination after migration: move the endpoint-specific
  suffix out of `base-url` and into a relative `Operation.path`, or remove
  `base-url` and use one absolute `Operation.path`.
- Request `url` or `path` has no effect: SCM Operation calls intentionally use
  the startup-resolved Provider base URL plus `Operation.path`. Request-level
  destination overrides remain available only to direct component calls.
- Customizer not executed: verify it appears in `scm.providers.<code>.message-customizers` and the `type` matches a registered factory.
- Unknown customizer type: add the factory Spring bean or fix the YAML `type`.
- Wrong order: set `message-customizers[].order` or adjust factory `defaultOrder()`.
- Missing Authorization header: verify `rest-auth-url.apply.location/name/format` and token response paths.
- Token not refreshed: check cache key components, `refresh-skew`, and auth response `expires-in-field`.
- Lock timeout: verify `LockUtility` and centralized cache are available across nodes.
- Centralized cache unavailable: configure `scm-cache-starter` and the cache named by `rest-auth-url.cache.name`.
