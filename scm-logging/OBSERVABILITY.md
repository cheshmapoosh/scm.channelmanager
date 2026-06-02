# Observability - scm-logging

## هدف

`scm-logging` برای Log Pipeline موجود استفاده می‌شود، اما در معماری جدید Trace/Log/Audit می‌توانند مستقیماً با Filebeat به Elasticsearch بروند.

## تصمیم

Audit با Application Log مخلوط نشود.

اگر در آینده Audit Store رسمی لازم شد، ماژول جداگانه `scm-audit-service` ساخته شود.

## Metric

Metric سرویس logging فقط از Actuator/Micrometer:

```text
/actuator/prometheus
```

Metricهای پیشنهادی:

```text
scm_logging_events_consumed_total
scm_logging_events_failed_total
scm_logging_persist_duration_seconds
```
