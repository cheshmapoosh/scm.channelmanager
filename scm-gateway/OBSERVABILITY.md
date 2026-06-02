# Observability - scm-gateway

## هدف

`scm-gateway` باید hookهای Observation برای پروتکل و Channel فراهم کند.

## مسئولیت‌ها

```text
correlationId generation/propagation
trace context propagation
gateway span enrichment
protocol result recording
SCMFault observability metadata
```

## Metric

Metric در این لایه فقط از طریق Micrometer ثبت می‌شود و از مسیر Actuator export می‌شود.

هیچ metricی در فایل نوشته نشود.

## ممنوع

- Audit persistence
- Provider-specific logic
- Metric file writing
