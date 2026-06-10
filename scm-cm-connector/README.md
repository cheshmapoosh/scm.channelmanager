# scm-cm-connector

`scm-cm-connector` connects the legacy CM system to SCM-zone services.

It uses:

- `scm-uaa-starter` for OAuth2 Resource Server security,
- `scm-cache-starter` for Hazelcast-backed cache infrastructure,
- `scm-observation-starter` for logs, traces, and metrics.

## Endpoints

```http
GET /internal/cm/v1/session
POST /internal/cm/v1/otp/verify
```

The session endpoint uses the authenticated `ScmPrincipal`. It never accepts nickname, terminal code, subject, session id, map name, cache key, or cache value from the request.

The OTP endpoint delegates to `OtpVerificationGateway`. The default gateway returns `501 Not Implemented`; it does not read OTP from cache and does not compare OTP locally.

## Session Cache

The connector receives `SessionCache` from `scm-uaa-starter` when security cache support is explicitly enabled:

```yaml
scm:
  security:
    cache:
      session:
        enabled: true
        cache-name: session_cache
      user:
        enabled: false

  cache:
    client:
      caches:
        session_cache:
          type: near
```

Ownership must be established before session data is returned.

## Observation

The connector records low-cardinality logs, traces, and metrics for session reads, cache access, ownership checks, OTP delegation, and error paths.

Never log or tag tokens, subjects, nicknames, terminal codes, session ids, JWT ids, raw cache keys, OTPs, passwords, or full cache values.
