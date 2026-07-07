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

Every LOG, TRACE, and AUDIT record includes:

```text
event.stream
scm.obs.target.namespace
scm.obs.target.index
scm.platform
service.name
deployment.environment
```

`scm.obs.target.index` is resolved dynamically from stream, namespace, environment, timestamp, and a real business `scm.channel.code` when present. Physical files are namespace-based and do not use channel code:

```text
{stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}.jsonl
```

CM connector spans are not legacy by default. If a request already started at `scm-web.gateway.receive`, connector spans should keep `scm.obs.legacy.enabled=false`. If CM connector is later configured as the direct business entry point for a legacy-reportable flow, only the root business span may set:

```text
scm.obs.legacy.enabled = true
scm.obs.legacy.operation.code
scm.obs.legacy.service.code
```

Console output for LOG, TRACE, and AUDIT is enabled only in `dev`. Test, pilot, and prod keep console disabled and file output enabled. Distributed tracing uses W3C `traceparent`; custom `X-SCM-*` trace headers are not distributed trace sources of truth.
