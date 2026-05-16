# scm-provider-rest

General-purpose REST provider module for Channel Manager.

This module is designed for `OperationType.PROVIDER` routes that should call external REST APIs with a simple map envelope.

Use provider URI:

```text
rest-provider:request?provider=hpsRest
```

Or directly in operation provider:

```text
rest-provider:request?provider=partnerA
```

## Request Envelope

Body can be a plain payload map, or an envelope with request metadata:

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
  },
  "auth": {
    "type": "BEARER",
    "token": "eyJ..."
  }
}
```

If no envelope keys are present, the whole body is used as request payload and defaults are applied from provider config.

## Response Shape

Provider returns a map:

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

## Configuration

```yaml
scm:
  cache:
    client:
      # for shared token cache across pods
      distributed: true
      default-type: remote
      utilities:
        lock: remote
        lock-names:
          rest-provider-token: remote
      caches:
        rest_provider_token_cache:
          type: remote

  provider:
    rest:
      enabled: true
      defaults:
        base-url: https://example.com
        connect-timeout-ms: 3000
        response-timeout-ms: 8000
        virtual-threads-enabled: true
        insecure-ssl: false
        follow-redirects: NORMAL # NEVER | NORMAL | ALWAYS
        default-method: POST
        headers:
          Accept: application/json
          Content-Type: application/json
        auth:
          type: NONE # NONE | BASIC | BEARER | JWT | API_KEY
          header-name: Authorization
          prefix: Bearer
          token: ""
          username: ""
          password: ""
          basic-base64: true
        token:
          enabled: false
          cache-name: rest_provider_token_cache
          cache-key: access-token
          lock-name: rest-provider-token
          early-refresh-seconds: 30
          default-expires-in-seconds: 300
          method: POST
          path: /connect/token
          headers:
            Content-Type: application/x-www-form-urlencoded
          form:
            grant_type: client_credentials
            client_id: cm_develop
            client_secret: ${HPS_CLIENT_SECRET}
          auth:
            type: NONE
          response-token-field: access_token
          response-expires-in-field: expires_in
          response-token-type-field: token_type
          default-token-type: Bearer
        security:
          sensitive-headers: [authorization, proxy-authorization, cookie, set-cookie]
          sensitive-body-keys: [password, token, secret, pin, cvv, pan, card]
          max-body-log-length: 400
      providers:
        hpsRest:
          base-url: https://hps.example.ir
          default-method: POST
          auth:
            type: BEARER
          token:
            enabled: true
            path: /oauth/token
            form:
              grant_type: client_credentials
              client_id: ${HPS_CLIENT_ID}
              client_secret: ${HPS_CLIENT_SECRET}
```

## Shared Access Token (Per Provider Instance)

When `token.enabled=true`, token retrieval is done with high-concurrency single-flight semantics:

1. Read token from shared cache (`cache-name`, `provider::cache-key`).
2. If token is valid (with `early-refresh-seconds` guard), reuse it.
3. If missing/near-expiry, acquire distributed lock (`lock-name::provider`).
4. Double-check cache inside lock, then call token endpoint once.
5. Store token in cache with TTL based on `expires_in`.

This allows all pods/threads to share the same provider token and avoid token endpoint stampede.

## Header Overrides

You can override metadata through exchange headers:

- `RestProvider` (provider name)
- `RestProviderMethod`
- `RestProviderUrl`
- `RestProviderPath`
- `RestProviderTimeoutMs`

## Observability

- Per-provider counters: submitted, succeeded, failed, client/server errors, timeout.
- OpenTelemetry client span is emitted per request with provider and HTTP attributes.
- Request/response debug logs are sanitized for sensitive headers and body keys.
