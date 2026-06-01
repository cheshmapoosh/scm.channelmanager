# Observability - scm-core

## هدف

`scm-core` محل instrumentation منطق Service، Operation، Policy و Routing است.

## Trace

نقاط هدف:

```text
service execution
operation execution
routing strategy
resilience policy
```

## Metric

Metricهای پیشنهادی:

```text
scm_service_executions_total
scm_operation_executions_total
scm_route_strategy_total
scm_resilience_rejections_total
```

Metric فقط Micrometer است، نه فایل.

## Audit

اگر API مدیریتی یا تغییر policy در core وجود داشت، Audit Event منتشر شود.
