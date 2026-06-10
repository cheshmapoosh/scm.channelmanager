# scm-uaa-starter

`scm-uaa-starter` provides SCM OAuth2 Resource Server security for application modules.

It does not depend on `scm-cache-starter`.

## Resource Server

Typical configuration:

```yaml
scm:
  security:
    resource-server:
      enabled: true
      issuer-uri: http://localhost:8080
      audiences:
        - scm-cm-connector
      required-claims:
        - sub
        - sid
        - nickname
        - terminalCode
      public-paths:
        - /actuator/health
        - /actuator/health/**
```

Configured public paths are permitted. Every other request is authenticated.

The starter validates issuer, standard JWT time claims, accepted audiences when configured, and required claims.

## Principal

JWTs are converted into `ScmPrincipal`:

```java
public record ScmPrincipal(
        String subject,
        String sessionId,
        String nickname,
        String terminalCode,
        String clientId,
        String tokenId,
        Set<String> roles,
        Set<String> scopes
) {
}
```

Use `ScmSecurityContext` to read the current principal.

## Ownership

Use `ScmOwnershipGuard` for resources that implement `ScmOwnedResource`.

Ownership is never granted silently when ownership metadata is missing. Subject, session id, and terminal code are checked when available on the resource.

## Security Cache

`scm-uaa-starter` can optionally create `SessionCache` and `UserCache` from Spring Cache abstractions:

```yaml
scm:
  security:
    cache:
      session:
        enabled: true
        cache-name: session_cache
      user:
        enabled: true
        cache-name: user_cache
```

It does not depend on `scm-cache-starter`. Applications that use Hazelcast-backed cache access should configure backend routing under `scm.cache.client.caches.*`.

Legacy authentication providers are disabled unless `scm.security.legacy-authentication.enabled=true`.

## Observation

The starter exposes security observation helpers when `scm-observation-starter` is active. Do not log or tag raw tokens, session ids, nicknames, terminal codes, JWT ids, OTPs, passwords, or credentials.
