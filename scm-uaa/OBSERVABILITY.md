# Observation در `scm-uaa`

## ۱. نقش ماژول

`scm-uaa` مالک جریان‌های authentication، token، account security و operationهای UAA است. این ماژول از هستهٔ Observation و Servlet adapter استفاده می‌کند:

```text
scm-observation-starter
scm-observation-servlet-starter
```

هستهٔ Observation مسئول registry، validation، sink و lifecycle است. UAA مسئول تعیین spanها، eventها، attributeهای اختصاصی و policy امنیتی خودش است.

## ۲. Signalهای مورد استفاده

| Signal | Policy |
| --- | --- |
| LOG | فعال برای رخدادهای امنیتی و عملیاتی امن |
| TRACE | فعال برای جریان‌های واقعی authentication و token |
| AUDIT | فقط برای operationهای قابل استناد و در صورت فعال‌سازی صریح |
| METRIC | از مسیر Actuator/Micrometer و در صورت فعال‌سازی |

Startup، bean initialization، Swagger، Actuator و endpointهای مدیریتی نباید به‌عنوان business trace ثبت شوند.

## ۳. Variableهای مشترک

| Variable | نمونه | کاربرد |
| --- | --- | --- |
| `SCM_APP` | `scm-uaa` | نام application |
| `SCM_ENV` | `dev` | محیط اجرا |
| `SCM_LABEL` | `master` | Config label |
| `SCM_METADATA_NAMESPACE` | `local` | namespace |
| `SCM_METADATA_INSTANCE_ID` | `local-scm-uaa` | شناسه instance |
| `SCM_METADATA_TIME_ZONE` | `Asia/Tehran` | timezone metadata |
| `SCM_OBS_ENABLED` | `true` | کلید اصلی Observation |
| `SCM_OBS_ROOT_DIR` | `/mnt/observation` | ریشه فایل‌ها |

## ۴. Variableهای signal و مقصد

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_OBS_LOG_ENABLED` | `true` | structured LOG |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | `true` در dev | console LOG |
| `SCM_OBS_LOG_CONSOLE_FORMAT` | `jsonl` | فرمت console |
| `SCM_OBS_LOG_FILE_ENABLED` | `true` | فایل LOG |
| `SCM_OBS_LOG_FILE_FORMAT` | `jsonl` | قرارداد Filebeat |
| `SCM_OBS_TRACE_ENABLED` | `true` | TRACE |
| `SCM_OBS_TRACE_CONSOLE_ENABLED` | `true` در dev | console TRACE |
| `SCM_OBS_TRACE_CONSOLE_FORMAT` | `jsonl` | فرمت console TRACE |
| `SCM_OBS_TRACE_FILE_ENABLED` | `true` | فایل TRACE |
| `SCM_OBS_TRACE_FILE_FORMAT` | `jsonl` | قرارداد Filebeat |
| `SCM_OBS_AUDIT_ENABLED` | `false` یا `true` | فعال‌سازی واقعی Audit |
| `SCM_OBS_AUDIT_CONSOLE_ENABLED` | `true` در dev | مقصد console؛ signal را فعال نمی‌کند |
| `SCM_OBS_METRIC_ENABLED` | `true` یا `false` | Metric |

## ۵. Variableهای Servlet observation

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_OBS_HTTP_SERVER_ENABLED` | `true` | ایجاد generic HTTP server span برای endpointهای انتخاب‌شده |
| `SCM_OBS_HTTP_SERVER_MODE` | `all` | policy ایجاد span |
| `SCM_OBS_HTTP_SERVER_SPAN_NAME` | `uaa.http.request` | نام span HTTP |

اگر فقط correlation/MDC لازم است و business span در lifecycle اختصاصی UAA ساخته می‌شود:

```bash
export SCM_OBS_HTTP_SERVER_ENABLED=false
export SCM_OBS_HTTP_SERVER_MODE=channel-only
```

## ۶. Variableهای اختصاصی UAA Observation

### ۶.۱. نگاشت client به channel

```text
SCM_UAA_OBS_CHANNEL_CLIENT_MAPPINGS
```

نمونه:

```bash
export SCM_UAA_OBS_CHANNEL_CLIENT_MAPPINGS='NIB=ib,IB=ib,internet=ib,PWA=mb,MB=mb,SA=mb'
```

هدف این variable تبدیل client شناخته‌شده به channel code پایدار است. client id خام نباید بدون نگاشت به‌عنوان channel code منتشر شود.

### ۶.۲. Legacy operation mapping

| Operation | Operation code variable | Service code variable | نمونه service code |
| --- | --- | --- | --- |
| Login | `SCM_UAA_OBS_LEGACY_LOGIN_OPERATION_CODE` | `SCM_UAA_OBS_LEGACY_LOGIN_SERVICE_CODE` | `UAA_LOGIN` |
| Change password | `SCM_UAA_OBS_LEGACY_CHANGE_PASSWORD_OPERATION_CODE` | `SCM_UAA_OBS_LEGACY_CHANGE_PASSWORD_SERVICE_CODE` | `UAA_CHANGE_PASSWORD` |
| Update favorite account | `SCM_UAA_OBS_LEGACY_UPDATE_FAVORITE_ACCOUNT_OPERATION_CODE` | `SCM_UAA_OBS_LEGACY_UPDATE_FAVORITE_ACCOUNT_SERVICE_CODE` | `UAA_UPDATE_FAVORITE_ACCOUNT` |
| Update account label | `SCM_UAA_OBS_LEGACY_UPDATE_ACCOUNT_LABEL_OPERATION_CODE` | `SCM_UAA_OBS_LEGACY_UPDATE_ACCOUNT_LABEL_SERVICE_CODE` | `UAA_UPDATE_ACCOUNT_LABEL` |

نمونه:

```bash
export SCM_UAA_OBS_LEGACY_LOGIN_OPERATION_CODE=login
export SCM_UAA_OBS_LEGACY_LOGIN_SERVICE_CODE=UAA_LOGIN

export SCM_UAA_OBS_LEGACY_CHANGE_PASSWORD_OPERATION_CODE=change-password
export SCM_UAA_OBS_LEGACY_CHANGE_PASSWORD_SERVICE_CODE=UAA_CHANGE_PASSWORD
```

Legacy code از نام span استخراج نمی‌شود. هر operation باید mapping صریح داشته باشد.

## ۷. نمونهٔ local

```bash
export SCM_APP=scm-uaa
export SCM_ENV=dev
export SCM_METADATA_NAMESPACE=local
export SCM_METADATA_INSTANCE_ID=local-scm-uaa

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=false

export SCM_OBS_LOG_CONSOLE_ENABLED=true
export SCM_OBS_TRACE_CONSOLE_ENABLED=true
export SCM_OBS_AUDIT_CONSOLE_ENABLED=true
export SCM_OBS_LOG_CONSOLE_FORMAT=jsonl
export SCM_OBS_TRACE_CONSOLE_FORMAT=jsonl

export SCM_OBS_HTTP_SERVER_ENABLED=false
export SCM_OBS_HTTP_SERVER_MODE=channel-only
export SCM_OBS_HTTP_SERVER_SPAN_NAME=uaa.http.request

export SCM_UAA_OBS_CHANNEL_CLIENT_MAPPINGS='NIB=ib,PWA=mb,MB=mb,SA=mb'
```

## ۸. نمونهٔ production

```bash
export SCM_APP=scm-uaa
export SCM_ENV=prod
export SCM_METADATA_NAMESPACE=scm-security
export SCM_METADATA_INSTANCE_ID=scm-uaa-0
export SCM_OBS_ROOT_DIR=/mnt/observation

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=true

export SCM_OBS_LOG_CONSOLE_ENABLED=false
export SCM_OBS_TRACE_CONSOLE_ENABLED=false
export SCM_OBS_AUDIT_CONSOLE_ENABLED=false

export SCM_OBS_LOG_FILE_ENABLED=true
export SCM_OBS_TRACE_FILE_ENABLED=true
export SCM_OBS_AUDIT_FILE_ENABLED=true
```

## ۹. Trace lifecycle

Span ورودی Login:

```text
uaa.auth.login
```

operationهای دیگر باید span یا eventی با semantic پایدار داشته باشند و فقط در نقطهٔ واقعی اجرای business ساخته شوند.

قواعد:

- `trace.id` از `traceparent` معتبر ادامه داده می‌شود؛
- `correlation.id` برای پردازش محلی UAA ساخته می‌شود؛
- child callهای واقعی span فرزند می‌گیرند؛
- failure قبل از بسته‌شدن span ثبت می‌شود؛
- exception ترجمه‌شده به response نیز باید outcome درست ایجاد کند.

## ۱۰. Security eventها

Eventهای خنثی security می‌توانند شامل موارد زیر باشند:

```text
AUTHENTICATION_STARTED
AUTHENTICATION_SUCCESS
AUTHENTICATION_FAILURE
ACCESS_DENIED
TOKEN_MISSING
TOKEN_INVALID
TOKEN_EXPIRED
```

Event payload فقط metadata امن و ثبت‌شده را حمل می‌کند. token یا credential نباید داخل event قرار گیرد.

## ۱۱. داده‌های مجاز و ممنوع

داده‌های امن نمونه:

```text
operation code
service code
channel code نگاشت‌شده
outcome
error.type
error.code
client type کنترل‌شده
```

داده‌های ممنوع:

```text
password
client secret
raw access token
raw refresh token
authorization code
Authorization header
Cookie
OTP
PIN
private key
full unrestricted claim map
```

شناسه یا username فقط در attribute ثبت‌شده و مطابق policy همان attribute منتشر می‌شود.

## ۱۲. مسیر توسعه

### افزودن operation جدید

1. نام operation و span/event آن را تعیین کنید؛
2. در صورت legacy projection دو variable برای operation code و service code اضافه کنید؛
3. default امن را در binding ماژول قرار دهید؛
4. variable را در این سند با مثال ثبت کنید؛
5. lifecycle موفقیت و شکست را در نقطهٔ واقعی operation متصل کنید.

### افزودن attribute جدید

1. attribute را در ماژول مالک با type و sensitivity مشخص تعریف کنید؛
2. آن را با `ObservationAttributeContributor` register کنید؛
3. فقط از نتیجهٔ authentication معتبر مقدار بگیرید؛
4. raw header یا token را parse نکنید؛
5. mapping Elasticsearch و مستند را به‌روزرسانی کنید.

### افزودن authentication mechanism جدید

- extraction باید از authentication معتبر framework انجام شود؛
- contributor اختصاصی mechanism ساخته شود؛
- attributeهای عمومی auth reuse شوند؛
- credential خام هرگز به Observation منتقل نشود.

## ۱۳. Metric

Metricها از مسیر زیر صادر می‌شوند:

```text
Actuator -> Micrometer -> /actuator/prometheus -> Prometheus -> Grafana
```

Tagها باید low-cardinality باشند. username، token id، correlation id، account و مقدار claim آزاد نباید metric tag شوند.
