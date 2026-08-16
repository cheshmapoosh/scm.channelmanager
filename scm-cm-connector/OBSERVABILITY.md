# Observation در `scm-cm-connector`

## ۱. نقش ماژول

`scm-cm-connector` پل HTTP بین CM قدیمی و سرویس‌های SCM است. این ماژول از:

```text
scm-observation-starter
scm-observation-servlet-starter
scm-uaa-starter
scm-cache-starter
```

استفاده می‌کند.

Starter اصلی قرارداد LOG، TRACE، AUDIT و METRIC را فراهم می‌کند. Servlet adapter مالک correlation و HTTP server observation است. CM Connector مالک spanها و eventهای مربوط به session read، ownership check، cache access و OTP delegation است.

## ۲. Signalهای مورد استفاده

| Signal | Policy |
| --- | --- |
| LOG | فعال |
| TRACE | فعال برای requestهای واقعی Connector |
| AUDIT | غیرفعال مگر برای operation قابل استناد آینده |
| METRIC | فعال در صورت نیاز عملیاتی |

## ۳. Variableهای مشترک

| Variable | نمونه | کاربرد |
| --- | --- | --- |
| `SCM_APP` | `scm-cm-connector` | نام application |
| `SCM_ENV` | `dev` | محیط اجرا |
| `SCM_LABEL` | `master` | Config label |
| `SCM_METADATA_NAMESPACE` | `local` | namespace |
| `SCM_METADATA_INSTANCE_ID` | `local-scm-cm-connector` | شناسه instance |
| `SCM_METADATA_TIME_ZONE` | `Asia/Tehran` | timezone metadata |
| `SCM_OBS_ENABLED` | `true` | کلید اصلی Observation |
| `SCM_OBS_ROOT_DIR` | `/mnt/observation` | ریشه فایل‌ها |

## ۴. Variableهای signal و مقصد

| Variable | نمونه |
| --- | --- |
| `SCM_OBS_LOG_ENABLED` | `true` |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | `true` در dev |
| `SCM_OBS_LOG_CONSOLE_FORMAT` | `jsonl` |
| `SCM_OBS_LOG_FILE_ENABLED` | `true` |
| `SCM_OBS_TRACE_ENABLED` | `true` |
| `SCM_OBS_TRACE_CONSOLE_ENABLED` | `true` در dev |
| `SCM_OBS_TRACE_CONSOLE_FORMAT` | `jsonl` |
| `SCM_OBS_TRACE_FILE_ENABLED` | `true` |
| `SCM_OBS_AUDIT_ENABLED` | `false` |
| `SCM_OBS_AUDIT_CONSOLE_ENABLED` | `true` در dev؛ به‌تنهایی Audit را فعال نمی‌کند |
| `SCM_OBS_METRIC_ENABLED` | `true` یا `false` |

## ۵. Variableهای Servlet observation

| Variable | Default/نمونه | توضیح |
| --- | --- | --- |
| `SCM_OBS_HTTP_SERVER_ENABLED` | `true` در صورت نیاز | ایجاد HTTP server span |
| `SCM_OBS_HTTP_SERVER_MODE` | `all` | policy مشاهده request |
| `SCM_OBS_HTTP_SERVER_SPAN_NAME` | `cm-connector.http.request` | نام span اختصاصی Connector |

نمونه:

```bash
export SCM_OBS_HTTP_SERVER_ENABLED=true
export SCM_OBS_HTTP_SERVER_MODE=all
export SCM_OBS_HTTP_SERVER_SPAN_NAME=cm-connector.http.request
```

## ۶. Variableهای Resource Server observation

| Variable | Default |
| --- | --- |
| `SCM_RESOURCE_SERVER_OBSERVATION_ENABLED` | `true` |
| `SCM_RESOURCE_SERVER_OBS_LOG_ENABLED` | `true` |
| `SCM_RESOURCE_SERVER_OBS_TRACE_ENABLED` | `true` |
| `SCM_RESOURCE_SERVER_OBS_METRIC_ENABLED` | `true` |

Variableهای امنیتی مرتبط:

| Variable | نمونه |
| --- | --- |
| `SCM_CM_CONNECTOR_RESOURCE_AUDIENCE` | `scm-cm-connector` |
| `SCM_CM_CONNECTOR_REQUIRED_CLAIM_SUB` | `sub` |
| `SCM_CM_CONNECTOR_REQUIRED_CLAIM_SESSION` | `sid` |
| `SCM_CM_CONNECTOR_REQUIRED_CLAIM_NICKNAME` | `nickname` |
| `SCM_CM_CONNECTOR_REQUIRED_CLAIM_TERMINAL` | `terminalCode` |

این variableها validation امنیتی را تنظیم می‌کنند؛ مقدار claim خام نباید به‌طور خودکار وارد Observation شود.

## ۷. نمونهٔ local

```bash
export SCM_APP=scm-cm-connector
export SCM_ENV=dev
export SCM_METADATA_NAMESPACE=local
export SCM_METADATA_INSTANCE_ID=local-scm-cm-connector

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=false

export SCM_OBS_LOG_CONSOLE_ENABLED=true
export SCM_OBS_TRACE_CONSOLE_ENABLED=true
export SCM_OBS_LOG_CONSOLE_FORMAT=jsonl
export SCM_OBS_TRACE_CONSOLE_FORMAT=jsonl

export SCM_OBS_HTTP_SERVER_ENABLED=true
export SCM_OBS_HTTP_SERVER_MODE=all
export SCM_OBS_HTTP_SERVER_SPAN_NAME=cm-connector.http.request

export SCM_RESOURCE_SERVER_OBSERVATION_ENABLED=true
export SCM_RESOURCE_SERVER_OBS_LOG_ENABLED=true
export SCM_RESOURCE_SERVER_OBS_TRACE_ENABLED=true
export SCM_RESOURCE_SERVER_OBS_METRIC_ENABLED=true
```

## ۸. Endpointها و lifecycle

```http
GET /internal/cm/v1/session
POST /internal/cm/v1/otp/verify
```

### Session

Trace و Log می‌توانند این مراحل را با metadata امن نشان دهند:

```text
request accepted
principal resolved
session cache read
ownership validated
response completed
```

### OTP delegation

Connector فقط delegation را observe می‌کند. OTP نباید از cache خوانده، compare یا log شود مگر implementation رسمی business آن صریحاً اضافه شود.

## ۹. مرز امنیتی

موارد زیر نباید در LOG، TRACE، AUDIT، event یا metric tag قرار گیرند:

```text
raw token
Authorization
subject خام بدون semantic ثبت‌شده
nickname آزاد
session id
JWT id
raw cache key
cache value
OTP
password
terminal credential
```

خطای cache یا security باید با `error.type` و `error.code` امن ثبت شود؛ exception message نامحدود مجاز نیست.

## ۱۰. Legacy projection

Connector به‌صورت پیش‌فرض legacy entry point نیست. spanهای technical یا requestهایی که trace آن‌ها از Gateway دیگری آمده است باید legacy projection را غیرفعال نگه دارند.

اگر در آینده CM Connector مستقیم یک business entry point شود:

1. root business span مشخص شود؛
2. service code و operation code از configuration صریح و variableهای اختصاصی ماژول تأمین شود؛
3. child spanها legacy نشوند؛
4. مستند operation و mapping legacy اضافه شود.

## ۱۱. Attribute registration

هر attribute اختصاصی Connector باید:

1. در کلاس attributeهای ماژول تعریف شود؛
2. type و sensitivity مشخص داشته باشد؛
3. از طریق `ObservationAttributeContributor` register شود؛
4. فقط از context معتبر application مقدار بگیرد؛
5. در این سند توضیح داده شود.

نام آزاد یا claim دلخواه بدون registration مجاز نیست.

## ۱۲. مسیر توسعه

### افزودن endpoint جدید

- نقطهٔ شروع و پایان lifecycle را مشخص کنید؛
- eventهای اصلی را محدود و semantic نگه دارید؛
- attributeهای جدید را register کنید؛
- variableهای policy جدید را در application binding و این سند اضافه کنید؛
- credential، cache payload و request body کامل را ثبت نکنید.

### افزودن cache operation

- نام cache به‌صورت finite metadata مجاز است؛
- raw key و value مجاز نیست؛
- hit/miss، duration، outcome و error code مناسب‌اند؛
- user/session identifiers نباید metric tag شوند.

### افزودن authentication mechanism

- از contributor مربوط به authentication معتبر استفاده کنید؛
- header یا token خام را parse نکنید؛
- failure Observation نباید security یا business flow را fail کند.
