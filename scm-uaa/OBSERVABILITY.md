# Observability - scm-uaa

## هدف

`scm-uaa` باید جریان احراز هویت و صدور token را قابل مشاهده کند.

## Trace

نقاط هدف:

```text
POST /oauth2/token
BaseGeneralAuthenticationProvider.authenticate
Token issue flow
Authentication failure handlers
```

Attributeهای پیشنهادی:

```text
scm.auth.success
scm.auth.failure_reason
scm.auth.request_type
scm.auth.component
scm.auth.method
```

## Log

لاگ‌های UAA باید JSON line باشند و این مقادیر را داشته باشند:

```text
correlationId
traceId
spanId
eventType
result
```

## Audit

رویدادهای پیشنهادی:

```text
AUTH_LOGIN_SUCCESS
AUTH_LOGIN_FAILURE
TOKEN_ISSUED
TOKEN_REJECTED
```

## Metric

Metric از فایل عبور نمی‌کند.

مسیر Metric:

```text
/actuator/prometheus -> Prometheus -> Grafana
```

Metricهای پیشنهادی:

```text
scm_auth_attempts_total
scm_auth_success_total
scm_auth_failure_total
scm_token_issued_total
```

## ممنوع

```text
password
client_secret
access_token
refresh_token
authorization_code
username خام
```
