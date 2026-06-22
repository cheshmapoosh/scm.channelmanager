# scm-cache

`scm-cache` is the SCM Hazelcast server/member process. It owns the embedded Hazelcast member bootstrap, server-side health checks, readiness/liveness probes, and Prometheus export.

`scm-cache-starter` remains the client-side cache integration module. Client cache metrics and auto-instrumentation are intentionally deferred to a later cycle.

## Ports

The service uses exactly two conceptual ports:

- `5701` named `hazelcast` for Hazelcast cluster/member communication.
- `8080` named `management` for the main Spring Boot web server and Actuator endpoints.

Actuator runs on the main web server port. Do not configure `management.server.port`, and do not add a separate actuator port.

## Actuator Endpoints

Only these Actuator endpoints are exposed:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
/actuator/info
/actuator/metrics
/actuator/prometheus
```

Probe aliases are also enabled on the same server port:

```text
/livez
/readyz
```

`/actuator/health/liveness` includes Spring Boot `livenessState` and `scmCacheHazelcastLiveness`. It checks that the application is alive and the embedded Hazelcast lifecycle service is running. It does not depend on the database or remote systems.

`/actuator/health/readiness` includes Spring Boot `readinessState` and the stable `scmCacheHazelcast` health indicator. Readiness checks that the embedded Hazelcast instance exists, its lifecycle service is running, bootstrap completed, registered element count matches materialized element count, and cluster size is at least `scm.cache.health.hazelcast.minimum-cluster-size` with a default of `1`.

The Hazelcast readiness details include:

```text
cluster.size
bootstrap.started
bootstrap.completed
registered.count
materialized.count
lastFailure.type
lastFailure.message
```

## Prometheus Metrics

Prometheus is available at:

```text
/actuator/prometheus
```

`scm-cache` registers low-cardinality gauges only:

```text
scm.cache.hazelcast.cluster.size
scm.cache.hazelcast.member.running
scm.cache.hazelcast.bootstrap.completed
scm.cache.hazelcast.elements.registered
scm.cache.hazelcast.elements.materialized
scm.cache.hazelcast.elements.registered.by.type
scm.cache.hazelcast.elements.materialized.by.type
scm.element.health
scm.element.risk
scm.element.materialized
scm.element.capacity.ratio
```

Allowed Hazelcast lifecycle tags are:

```text
service=scm-cache
element.type=<HazelcastElementType>
```

Allowed generic element metric tags are:

```text
component=hazelcast
element_type=<HazelcastElementType>
element_name=<finite configured element name>
```

Element names are allowed as metric tags only for finite configured Hazelcast elements. Never use request, user, account, token, correlation, or other dynamic values as element tags.

Element risk is calculated inside the application before metrics are exported. Grafana and Prometheus must alert on the final values, not recalculate thresholds:

```text
scm.element.risk: 0=normal, 1=warning, 2=critical
scm.element.health: 1=healthy, 0=unhealthy
```

`warningRatio` and `criticalRatio` are capacity ratios between `0` and `1`. `scm-observation-starter` owns `ScmElementRiskProperties`, `ScmElementRiskEngine`, and `ScmElementHealthEngine`; `scm-cache` binds them with `scm.cache.health.hazelcast.element-risk` and provides Hazelcast samples. Grafana should alert on `scm.element.risk == 1`, `scm.element.risk == 2`, and `scm.element.health == 0`.

## Structured Logs

`ScmCacheInitLogging` is startup-log-only. It creates the initial lifecycle correlation id and minimal structured arguments before the normal Spring observation API is fully available. It must not contain business logic, transaction logic, repository access, health logic, or Hazelcast bootstrap logic.

Startup/context/init logs use explicit `correlation.type=lifecycle`; they must not fall back to `unknown`.

Event categories:

```text
scm.cache.context
scm.cache.init
scm.cache.health
```

Stable event actions:

```text
runtime.context.created
scm.cache.init.started
hazelcast.bootstrap.started
hazelcast.bootstrap.config.loaded
hazelcast.bootstrap.config.registered
hazelcast.bootstrap.member.started
hazelcast.bootstrap.objects.materialized
hazelcast.bootstrap.completed
hazelcast.bootstrap.failed
hazelcast.element.registered
hazelcast.element.materialized
hazelcast.health.changed
```

Hazelcast bootstrap logs include loaded definition count, registered element count and summary by type, one registered event per element, materialized element count and summary by type, one materialized event per element, member address, and cluster size. Do not log cache keys, cache values, request bodies, tokens, OTPs, passwords, kubeconfig, full network configuration, or sensitive command-line arguments.

Registered and materialized Hazelcast element logs include `cache.hazelcast.element.type`, `cache.hazelcast.element.name`, and one LOG-only text field named `cache.hazelcast.element.config`. The config text uses deterministic key order, excludes nulls, and includes only selected safe values such as `ttlSeconds`, `maxIdleSeconds`, backup counts, `statisticsEnabled`, `evictionSize`, and `evictionMaxSizePolicy`. Nested JSON config logging is intentionally not used, and full Hazelcast config objects are never logged. `scm-cache` log attributes are registered through `ScmCacheObservationAttributeContributor`.

## Configuration Source

Local development uses module-local Spring configuration under:

```text
scm-cache/src/main/resources/
```

Non-development environments must use Spring Cloud Config through `scm-config`. Do not add a centralized root `docs/` folder for module-specific observability details.

## Enabled Signals

SCM Cache uses:

```text
LOG    = enabled
TRACE  = enabled
AUDIT  = disabled
METRIC = enabled
```

Metrics follow the standard Actuator and Micrometer flow:

```text
Metric -> Actuator -> Micrometer -> Prometheus -> Grafana
```

Do not write metrics to files.
