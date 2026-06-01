# Observability - scm-provider-rest

## هدف

Provider REST باید callهای خروجی را trace و metric کند.

## Trace

```text
scm.provider.call
```

Attributeهای مجاز:

```text
scm.provider.code
scm.service.code
scm.operation.code
scm.result
scm.error.code
```

## Metric

Metric از Micrometer:

```text
scm_provider_calls_total
scm_provider_call_duration_seconds
scm_provider_errors_total
scm_provider_timeouts_total
```

## ممنوع

- request/response کامل بدون masking
- token
- credential
- metric file writing
