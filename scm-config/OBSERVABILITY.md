# Observability - scm-config

## هدف

`scm-config` باید تغییرات config را audit کند.

هستهٔ `scm-observation-starter` transport-neutral است. `scm-config` برای correlation مربوط به Servlet request و HTTP server observation به‌صورت صریح از `scm-observation-servlet-starter` استفاده می‌کند؛ attributeهای HTTP در adapter ساخته می‌شوند.

## Audit Events

```text
CONFIG_CREATED
CONFIG_UPDATED
CONFIG_DELETED
CONFIG_VERSION_PUBLISHED
CONFIG_ROLLBACK
```

## Log

تغییرات config در Log نیز با correlationId و traceId ثبت شوند، اما Log جای Audit نیست.

## Metric

Metric از Actuator:

```text
/actuator/prometheus
```

Metricهای پیشنهادی:

```text
scm_config_requests_total
scm_config_changes_total
scm_config_errors_total
```
