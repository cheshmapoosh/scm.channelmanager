# scm-observability-api

## هدف

این ماژول قراردادها و constantهای مشترک Observability را نگه می‌دارد.

این ماژول باید سبک، مستقل و بدون وابستگی به Spring Boot، OpenTelemetry SDK، Micrometer Registry، Logback، JPA یا MQ باشد.

## مسئولیت‌ها

```text
ScmTraceAttributes
ScmMdcKeys
ScmMetricNames
ScmObservationType
ScmObservationResult
ScmSensitiveFields
```

## نکته درباره Metric

`ScmMetricNames` فقط نام metricها را نگه می‌دارد.  
Metric نباید در فایل ذخیره شود.

مسیر Metric:

```text
Actuator -> Micrometer -> Prometheus -> Grafana
```

## ممنوع

- Spring Boot AutoConfiguration
- File writer
- JPA Entity
- MQ Publisher/Consumer
- Logback configuration
