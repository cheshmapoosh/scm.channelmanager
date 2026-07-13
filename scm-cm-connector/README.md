# `scm-cm-connector`

`scm-cm-connector` پل HTTP بین CM قدیمی و سرویس‌های SCM است.

وابستگی‌های اصلی:

```text
scm-uaa-starter
scm-cache-starter
scm-observation-starter
scm-observation-servlet-starter
```

مستند کامل Observation، variableها، lifecycle و مسیر توسعه:

```text
scm-cm-connector/OBSERVABILITY.md
```

## Endpointها

```http
GET /internal/cm/v1/session
POST /internal/cm/v1/otp/verify
```

Endpoint مربوط به session فقط از `ScmPrincipal` معتبر استفاده می‌کند. nickname، terminal code، subject، session id، map name، cache key یا cache value نباید از request به‌عنوان منبع قابل اعتماد پذیرفته شود.

Endpoint مربوط به OTP به `OtpVerificationGateway` واگذار می‌شود. implementation پیش‌فرض `501 Not Implemented` برمی‌گرداند و OTP را از cache نمی‌خواند یا به‌صورت محلی مقایسه نمی‌کند.

## Session Cache

فعال‌سازی session cache فقط از طریق variable انجام می‌شود:

```bash
export SCM_SECURITY_SESSION_CACHE_ENABLED=true
export SCM_SECURITY_SESSION_CACHE_NAME=session_cache
export SCM_SECURITY_USER_CACHE_ENABLED=false

export SCM_CACHE_DISTRIBUTED=true
export SCM_CACHE_SESSION_TYPE=near
export SCM_CACHE_CLUSTER_NAME=scm-cache
export SCM_CACHE_ADDRESS=127.0.0.1:5701
```

قبل از بازگرداندن session، ownership باید از principal معتبر اثبات شود.

## Resource Server

نمونهٔ variableهای اصلی:

```bash
export SCM_RESOURCE_SERVER_ENABLED=true
export SCM_UAA_BASE_URL=http://localhost:8000
export SCM_CM_CONNECTOR_RESOURCE_AUDIENCE=scm-cm-connector
export SCM_RESOURCE_SERVER_METHOD_SECURITY_ENABLED=true
```

Claimهای required نیز با variableهای زیر تعیین می‌شوند:

```text
SCM_CM_CONNECTOR_REQUIRED_CLAIM_SUB
SCM_CM_CONNECTOR_REQUIRED_CLAIM_SESSION
SCM_CM_CONNECTOR_REQUIRED_CLAIM_NICKNAME
SCM_CM_CONNECTOR_REQUIRED_CLAIM_TERMINAL
```

## Observation

تنظیمات runtime Observation با environment variable انجام می‌شود، نه با property مستقیم. نمونهٔ local:

```bash
export SCM_APP=scm-cm-connector
export SCM_ENV=dev
export SCM_METADATA_NAMESPACE=local
export SCM_METADATA_INSTANCE_ID=local-scm-cm-connector

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=false

export SCM_OBS_HTTP_SERVER_ENABLED=true
export SCM_OBS_HTTP_SERVER_MODE=all
export SCM_OBS_HTTP_SERVER_SPAN_NAME=cm-connector.http.request
```

جزئیات کامل در `OBSERVABILITY.md` قرار دارد.

## قواعد امنیتی

موارد زیر نباید log، trace، audit یا metric tag شوند:

```text
raw token
Authorization
session id
raw cache key
cache value
OTP
password
credential
```
