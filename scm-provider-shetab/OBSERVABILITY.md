# Observability - scm-provider-shetab

## هدف

Provider Shetab باید callهای TCP/ISO را trace و metric کند بدون اینکه اطلاعات حساس کارت یا پیام خام ثبت شود.

## Trace

Span پیشنهادی:

```text
scm.provider.call
```

## Metric

```text
scm_provider_calls_total
scm_provider_call_duration_seconds
scm_provider_timeouts_total
```

Tagهای مجاز:

```text
provider
operation
result
error_code
mti
```

## ممنوع

```text
PAN کامل
CVV2
PIN
Track data
ISO message خام
metric file writing
```
