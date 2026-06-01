# scm-observability-spring-boot-starter

## هدف

این ماژول AutoConfiguration مشترک برای ماژول‌های Spring Boot پروژه SCM است.

## مسئولیت‌ها

```text
CorrelationIdFilter
MdcFilter
Trace file sink auto-configuration
Audit publisher auto-configuration
Log sanitization support
Actuator/Micrometer/Prometheus support
```

## تصمیم Metric

Metric از مسیر استاندارد زیر فعال می‌شود:

```text
Spring Boot Actuator
    ↓
Micrometer
    ↓
/actuator/prometheus
    ↓
Prometheus
    ↓
Grafana
```

Metric نباید وارد `scm-observability-file-sink` شود.

## Properties پیشنهادی

```yaml
scm:
  observability:
    enabled: true
    tracing:
      file-enabled: true
    logging:
      mdc-enabled: true
      file-enabled: true
      sanitize-enabled: true
    audit:
      file-enabled: true
    metrics:
      enabled: true
```

## وابستگی‌های پیشنهادی

```text
scm-observability-api
scm-observability-file-sink
scm-audit-client
Spring Boot Actuator
Micrometer
Prometheus registry
```
