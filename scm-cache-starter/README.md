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

## Ownership

| Cache type | Storage | Allowed per-cache properties | TTL owner | Size owner |
| ---------- | ------- | ---------------------------- | --------- | ---------- |
| `LOCAL` | Caffeine inside the application JVM | `ttl`, `maximum-size` | Host application | Host application |
| `REMOTE` | Hazelcast cluster | `remote-name` | `scm-cache` database configuration | `scm-cache` database configuration |
| `NEAR` | Hazelcast Map plus client-side Near Cache | `remote-name`, `maximum-size` | Remote TTL: `scm-cache` database configuration | Near size: host application; remote size: `scm-cache` |

For `REMOTE` and `NEAR`, remote Map TTL comes from `TBL_CHE_INSTANCE_CONFIG.TTL_SECONDS` and remote Map capacity comes from `TBL_CHE_INSTANCE_CONFIG.EVIC_SIZE`. The host application must not duplicate these settings in YAML.

Near Cache `maximum-size` controls only the local Near Cache of each application JVM or Pod. It does not change the remote Hazelcast Map eviction size.

`LOCAL` caches are not shared between Pods. `REMOTE` and `NEAR` caches require the remote Hazelcast cluster.

## Per-Cache Examples

Local cache with host-owned TTL and capacity:

```yaml
scm:
  cache:
    client:
      caches:
        my-local-cache:
          type: local
          ttl: 5m
          maximum-size: 1000
```

Remote cache with server-owned TTL and capacity:

```yaml
scm:
  cache:
    client:
      caches:
        session_cache:
          type: remote
          remote-name: session_cache
```

Near Cache with host-owned Near Cache capacity and server-owned remote Map settings:

```yaml
scm:
  cache:
    client:
      near:
        enabled: true
        maximum-size: 10000
        invalidate-on-change: true
        in-memory-format: OBJECT
      caches:
        user_cache:
          type: near
          remote-name: user_cache
          maximum-size: 5000
```

When a `LOCAL` cache omits per-cache values, `local.ttl` and `local.maximum-size` are used. A zero or negative local TTL means no expiration.

When a `NEAR` cache omits per-cache `maximum-size`, `near.maximum-size` is used.

## Validation

Invalid cache definitions fail during application startup.

Allowed per-cache properties:

| Effective type | Allowed per-cache properties |
| -------------- | ---------------------------- |
| `LOCAL` | `type`, `ttl`, `maximum-size` |
| `REMOTE` | `type`, `remote-name` |
| `NEAR` | `type`, `remote-name`, `maximum-size` |

Validation uses the effective type. If a cache definition does not set `type`, `default-type` is used before validation.

Additional rules:

- `maximum-size` must be positive when specified.
- `remote-name` must not be blank when specified.
- `LOCAL` caches must not define `remote-name`.
- `REMOTE` and `NEAR` caches must not define YAML `ttl`.
- `REMOTE` caches must not define host-side `maximum-size`.
- `NEAR` caches require `distributed=true` and `near.enabled=true`.

## Programmatic TTL

Explicit per-entry TTL supplied by application code remains supported:

```java
cache.put(key, value, Duration.ofMinutes(5));
```

This is an operation-level override. Normal Spring Cache operations such as `put(key, value)` do not send an explicit TTL for `REMOTE` or `NEAR`; the remote entry TTL is owned by the `scm-cache` server-side `MapConfig`.

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
          maximum-size: 10000
        user_cache:
          type: near
          remote-name: user_cache
          maximum-size: 10000
```

`session_cache` and `user_cache` must not use `LOCAL`.

## Configuration Breaking Change

The old pattern below is intentionally rejected:

```yaml
session_cache:
  type: near
  ttl: 30m
  maximum-size: 10000
```

Remove `ttl` from `REMOTE` and `NEAR` cache definitions. Configure remote Map TTL in `scm-cache` through `TBL_CHE_INSTANCE_CONFIG.TTL_SECONDS`.

For `scm-web`, the session and user capacity environment variables were renamed to make the Near Cache scope explicit:

- Removed: `SCM_CACHE_SESSION_TTL`
- Removed: `SCM_CACHE_USER_TTL`
- Renamed: `SCM_CACHE_SESSION_MAXIMUM_SIZE` -> `SCM_CACHE_SESSION_NEAR_MAXIMUM_SIZE`
- Renamed: `SCM_CACHE_USER_MAXIMUM_SIZE` -> `SCM_CACHE_USER_NEAR_MAXIMUM_SIZE`

No deprecated YAML TTL alias is retained for `REMOTE` or `NEAR`, because that would keep two sources of truth for remote entry TTL.

## Observation

When `scm-observation-starter` is present and enabled, the starter exposes lightweight cache observation helpers. Metrics still use Micrometer; they are not written to files.

Never use cache keys, user identifiers, request IDs, correlation IDs, OTPs, tokens, exception messages, or cache values as metric tags.
