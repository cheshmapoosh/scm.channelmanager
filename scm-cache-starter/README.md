# scm-cache-starter

`scm-cache-starter` provides Spring Boot client-side cache access for application modules.

It is not used by `scm-cache`, which is the Hazelcast member process.

## Core Client

Core client behavior is configured under:

```yaml
scm:
  cache:
    client:
      distributed: true
      default-type: remote
      remote:
        cluster-name: scm-cache-dev
        addresses:
          - 127.0.0.1:5701
      local:
        ttl: 0s
        maximum-size: 10000
      near:
        enabled: true
        maximum-size: 10000
        invalidate-on-change: true
        in-memory-format: OBJECT
```

The default mode is a remote distributed Hazelcast client. Embedded Hazelcast is used only when explicitly configured with `scm.cache.client.distributed=false`.

Backend names are stable public contract names:

- `LOCAL` is process-local Caffeine.
- `REMOTE` is a Hazelcast remote map.
- `NEAR` is a Hazelcast remote map with Hazelcast Near Cache enabled on the client side.

Do not configure provider selection under `local` or `remote`; provider selection is not public configuration.

## Security Caches

`scm-cache-starter` does not create `SessionCache`, `UserCache`, or security cache helper beans.

Security cache abstractions live in `scm-uaa-common`. Optional Resource Server auto-configuration lives in `scm-uaa-starter`.

Backend routing for security caches still belongs under the cache client namespace:

```yaml
scm:
  cache:
    client:
      caches:
        session_cache:
          type: near
          remote-name: session_cache
          ttl: 30m
          maximum-size: 10000
        user_cache:
          type: near
          remote-name: user_cache
          ttl: 30m
          maximum-size: 10000
```

Do not use local-only cache for `session_cache` or `user_cache`.

## Observation

When `scm-observation-starter` is present and enabled, the starter exposes lightweight cache observation helpers. Metrics still use Micrometer; they are not written to files.

Never use cache keys, user identifiers, request IDs, correlation IDs, OTPs, tokens, exception messages, or cache values as metric tags.
