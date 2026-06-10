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

## Request And Response

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

- Customizer not executed: verify it appears in `scm.providers.<code>.message-customizers` and the `type` matches a registered factory.
- Unknown customizer type: add the factory Spring bean or fix the YAML `type`.
- Wrong order: set `message-customizers[].order` or adjust factory `defaultOrder()`.
- Missing Authorization header: verify `rest-auth-url.apply.location/name/format` and token response paths.
- Token not refreshed: check cache key components, `refresh-skew`, and auth response `expires-in-field`.
- Lock timeout: verify `LockUtility` and centralized cache are available across nodes.
- Centralized cache unavailable: configure `scm-cache-starter` and the cache named by `rest-auth-url.cache.name`.
