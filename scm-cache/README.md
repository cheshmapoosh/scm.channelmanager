# `scm-cache`

`scm-cache` فرایند server/member مربوط به Hazelcast در SCM است و مسئول bootstrap عضو Hazelcast، persistence configuration، health، readiness/liveness و Prometheus export است.

`scm-cache-starter` ماژول client-side cache integration است و مسئولیت جداگانه دارد.

مستند کامل Observation، variableها، signalها و مسیر توسعه:

```text
scm-cache/OBSERVABILITY.md
```

## Portها

```text
5701  hazelcast    ارتباط cluster/member
8080  management   Spring Boot و Actuator
```

Port `8080` فقط برای management است و business API یا direct cache-management API ارائه نمی‌کند. Actuator روی همان port اجرا می‌شود و `management.server.port` جداگانه نباید تعریف شود.

## Actuator

Endpointهای مجاز:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
/actuator/info
/actuator/metrics
/actuator/prometheus
/livez
/readyz
```

Liveness فقط زنده‌بودن application و lifecycle عضو Hazelcast را بررسی می‌کند.

Readiness موارد زیر را بررسی می‌کند:

```text
Hazelcast instance موجود باشد
lifecycle service در حال اجرا باشد
bootstrap کامل شده باشد
registered count با materialized count سازگار باشد
cluster size از حداقل پیکربندی کمتر نباشد
```

حداقل cluster size با variable زیر تنظیم می‌شود:

```bash
export SCM_CACHE_HEALTH_MIN_CLUSTER_SIZE=1
```

## Prometheus Metrics

Metricها از این مسیر صادر می‌شوند:

```text
/actuator/prometheus
```

Metricهای اصلی:

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

Tagهای مجاز low-cardinality هستند:

```text
service=scm-cache
component=hazelcast
element_type=<HazelcastElementType>
element_name=<finite configured element name>
```

request، user، account، token، correlation id و مقادیر آزاد نباید metric tag شوند.

## Risk Policy

Variableهای اصلی:

```bash
export SCM_CACHE_ELEMENT_WARNING_RATIO=0.80
export SCM_CACHE_ELEMENT_CRITICAL_RATIO=0.90
export SCM_CACHE_USER_WARNING_RATIO=0.80
export SCM_CACHE_USER_CRITICAL_RATIO=0.90
```

معنای metricها:

```text
scm.element.risk:   0=normal, 1=warning, 2=critical
scm.element.health: 1=healthy, 0=unhealthy
```

محاسبهٔ risk و health داخل application انجام می‌شود. Dashboard نباید thresholdها را دوباره محاسبه کند.

## Hazelcast Runtime Variables

نمونهٔ local:

```bash
export SCM_CACHE_CLUSTER_NAME=scm-cache-dev
export SCM_CACHE_MEMBER_NAME=scm-cache-dev-local
export SCM_CACHE_NETWORK_PORT=5701
export SCM_CACHE_NETWORK_PORT_AUTO_INCREMENT=true
export SCM_CACHE_NETWORK_PORT_COUNT=100
export SCM_CACHE_TCP_IP_ENABLED=false
export SCM_CACHE_KUBERNETES_ENABLED=false
```

نمونهٔ Kubernetes:

```bash
export SCM_CACHE_KUBERNETES_ENABLED=true
export SCM_CACHE_KUBERNETES_NAMESPACE=scm-platform
export SCM_CACHE_KUBERNETES_SERVICE_NAME=scm-cache
```

## Observation

`scm-cache` فقط از هستهٔ transport-neutral استفاده می‌کند و Servlet adapter ندارد. endpointهای Actuator business trace ایجاد نمی‌کنند.

نمونهٔ variableهای Observation:

```bash
export SCM_APP=scm-cache
export SCM_ENV=prod
export SCM_METADATA_NAMESPACE=scm-platform
export SCM_METADATA_INSTANCE_ID=scm-cache-0

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=false
export SCM_OBS_METRIC_ENABLED=true

export SCM_OBS_LOG_FILE_ENABLED=true
export SCM_OBS_TRACE_FILE_ENABLED=true
```

برای جزئیات lifecycle، eventها، attributeها و مسیر توسعه به `OBSERVABILITY.md` مراجعه شود.

## مرز امنیتی

موارد زیر نباید در output ثبت شوند:

```text
cache key
cache value
request body
token
OTP
password
kubeconfig
full network configuration
sensitive command-line argument
```

Config element فقط به‌صورت خلاصهٔ deterministic و allowlisted ثبت می‌شود.
