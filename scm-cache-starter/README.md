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
      config:
        cluster-name: dev3
        network-config:
          addresses:
            - 127.0.0.1:5701
```

The default mode is a remote distributed Hazelcast client. Embedded Hazelcast is used only when explicitly configured with `scm.cache.client.distributed=false`.

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
        user_cache:
          type: near
```

Do not use local-only cache for `session_cache` or `user_cache`.

## Observation

When `scm-observation-starter` is present and enabled, the starter exposes lightweight cache observation helpers. Metrics still use Micrometer; they are not written to files.

Never use cache keys, user identifiers, request IDs, correlation IDs, OTPs, tokens, exception messages, or cache values as metric tags.
