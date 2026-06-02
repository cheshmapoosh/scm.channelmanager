# Observability - scm-web

## هدف

`scm-web` نقطه اصلی Gateway runtime است و باید Observation سطح Channel، Service، Operation و Provider را فعال کند.

## Trace

Spanهای پیشنهادی:

```text
scm.channel.receive
scm.service.execute
scm.operation.execute
scm.provider.call
```

## Log

خروجی Log باید JSON line باشد و توسط Filebeat خوانده شود.

مسیر پیشنهادی:

```text
/log/scm/scm-web/application.ndjson
```

## Audit

رویدادهای پیشنهادی:

```text
BUSINESS_OPERATION_EXECUTED
BUSINESS_OPERATION_DENIED
PROVIDER_SELECTED
ROUTE_CHANGED
```

## Metric

Metric فقط از Actuator/Micrometer:

```text
/actuator/prometheus
```

Metricهای پیشنهادی:

```text
scm_gateway_requests_total
scm_gateway_request_duration_seconds
scm_gateway_errors_total
```
