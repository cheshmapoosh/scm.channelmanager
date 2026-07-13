# Observation در `scm-config`

## ۱. نقش ماژول

`scm-config` میزبان Spring Cloud Config Server است. این ماژول از:

```text
scm-observation-starter
scm-observation-servlet-starter
```

استفاده می‌کند.

Starter هستهٔ signalها، registry و خروجی را فراهم می‌کند. Servlet adapter مسئول correlation و HTTP server observation است. این ماژول فقط policy و eventهای اختصاصی Config Server را مالک است.

## ۲. Signalهای مورد استفاده

| Signal | Policy |
| --- | --- |
| LOG | فعال |
| TRACE | فعال برای HTTP requestهای Config Server در صورت فعال‌سازی HTTP span |
| AUDIT | برای تغییرات قابل استناد Config باید به‌صورت صریح فعال شود |
| METRIC | اختیاری و از مسیر Actuator/Micrometer |

Audit جای Log نیست. Log برای عملیات روزمره و خطاهای runtime است؛ Audit برای ثبت تغییر قابل استناد استفاده می‌شود.

## ۳. Variableهای مشترک

| Variable | نمونه | کاربرد |
| --- | --- | --- |
| `SCM_APP` | `scm-config` | نام application |
| `SCM_ENV` | `dev` | محیط اجرا |
| `SCM_LABEL` | `master` | label پیکربندی |
| `SCM_METADATA_NAMESPACE` | `local` | namespace |
| `SCM_METADATA_INSTANCE_ID` | `local-scm-config` | شناسه instance |
| `SCM_METADATA_TIME_ZONE` | `Asia/Tehran` | timezone metadata |
| `SCM_OBS_ENABLED` | `true` | کلید اصلی Observation |
| `SCM_OBS_ROOT_DIR` | `/mnt/observation` | ریشه فایل‌ها |

## ۴. Variableهای signal و مقصد

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_OBS_LOG_ENABLED` | `true` | فعال‌سازی structured LOG |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | `true` در dev | خروجی console LOG |
| `SCM_OBS_LOG_CONSOLE_FORMAT` | `jsonl` | فرمت console |
| `SCM_OBS_LOG_FILE_ENABLED` | `true` | خروجی فایل LOG |
| `SCM_OBS_LOG_FILE_FORMAT` | `jsonl` | قرارداد Filebeat |
| `SCM_OBS_TRACE_ENABLED` | `true` | فعال‌سازی TRACE |
| `SCM_OBS_TRACE_CONSOLE_ENABLED` | `true` در dev | خروجی console TRACE |
| `SCM_OBS_TRACE_FILE_ENABLED` | `true` | خروجی فایل TRACE |
| `SCM_OBS_TRACE_FILE_FORMAT` | `jsonl` | قرارداد Filebeat |
| `SCM_OBS_AUDIT_ENABLED` | `true` | فعال‌سازی واقعی Audit تغییرات Config |
| `SCM_OBS_AUDIT_CONSOLE_ENABLED` | `true` در dev | فقط مقصد console؛ signal را فعال نمی‌کند |
| `SCM_OBS_AUDIT_FILE_ENABLED` | `true` | خروجی فایل Audit |
| `SCM_OBS_AUDIT_FILE_FORMAT` | `jsonl` | قرارداد Filebeat |
| `SCM_OBS_METRIC_ENABLED` | `true` یا `false` | فعال‌سازی metricهای ماژول |

## ۵. Variableهای HTTP observation

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_OBS_HTTP_SERVER_ENABLED` | `true` | ایجاد generic HTTP server span |
| `SCM_OBS_HTTP_SERVER_MODE` | `all` | مشاهدهٔ requestهای HTTP مجاز |
| `SCM_OBS_HTTP_SERVER_SPAN_NAME` | `config.http.request` | نام span اختصاصی ماژول |

در صورتی که فقط correlation/MDC لازم باشد و span عمومی HTTP نباید ساخته شود:

```bash
export SCM_OBS_HTTP_SERVER_ENABLED=false
export SCM_OBS_HTTP_SERVER_MODE=channel-only
```

## ۶. Variableهای level

| Variable | نمونه |
| --- | --- |
| `SCM_LOG_LEVEL_ROOT` | `INFO` |
| `SCM_LOG_LEVEL_APPLICATION` | `INFO` |
| `SCM_LOG_LEVEL_SPRING` | `INFO` |
| `SCM_LOG_LEVEL_HIBERNATE` | `WARN` |
| `SCM_LOG_LEVEL_HAZELCAST` | `INFO` |

## ۷. نمونهٔ local

```bash
export SCM_APP=scm-config
export SCM_ENV=dev
export SCM_METADATA_NAMESPACE=local
export SCM_METADATA_INSTANCE_ID=local-scm-config

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=true
export SCM_OBS_METRIC_ENABLED=false

export SCM_OBS_LOG_CONSOLE_ENABLED=true
export SCM_OBS_TRACE_CONSOLE_ENABLED=true
export SCM_OBS_AUDIT_CONSOLE_ENABLED=true
export SCM_OBS_LOG_CONSOLE_FORMAT=jsonl
export SCM_OBS_TRACE_CONSOLE_FORMAT=jsonl
export SCM_OBS_AUDIT_CONSOLE_FORMAT=jsonl

export SCM_OBS_HTTP_SERVER_ENABLED=true
export SCM_OBS_HTTP_SERVER_MODE=all
export SCM_OBS_HTTP_SERVER_SPAN_NAME=config.http.request
```

## ۸. نمونهٔ production

```bash
export SCM_APP=scm-config
export SCM_ENV=prod
export SCM_METADATA_NAMESPACE=scm-platform
export SCM_METADATA_INSTANCE_ID=scm-config-0
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

## ۹. Audit eventهای Config

Eventهای دامنهٔ Config باید نام پایدار داشته باشند:

```text
CONFIG_CREATED
CONFIG_UPDATED
CONFIG_DELETED
CONFIG_VERSION_PUBLISHED
CONFIG_ROLLBACK
```

هر event باید فقط metadata امن و ثبت‌شده را حمل کند؛ برای مثال:

```text
config.application
config.profile
config.label
config.action
config.version
actor.id
request correlation
outcome
```

محتوای کامل فایل configuration، password، token، key، credential و مقدار secret نباید در Audit یا Log قرار گیرد.

## ۱۰. مسیر توسعه

برای افزودن یک Audit یا attribute جدید:

1. نام semantic و owner را در ماژول `scm-config` مشخص کنید؛
2. attribute را با type و sensitivity دقیق تعریف کنید؛
3. یک `ObservationAttributeContributor` میزبان ارائه کنید؛
4. event را در نقطهٔ واقعی موفقیت یا شکست business منتشر کنید؛
5. مقدار secret یا payload کامل را وارد event نکنید؛
6. variable جدید فقط در صورت وجود policy runtime جدید اضافه شود؛
7. همین سند و mapping downstream به‌روزرسانی شود.

## ۱۱. Metric

Metricها از مسیر زیر منتشر می‌شوند:

```text
Actuator -> Micrometer -> /actuator/prometheus -> Prometheus -> Grafana
```

نام‌های پیشنهادی:

```text
scm_config_requests_total
scm_config_changes_total
scm_config_errors_total
```

Tagها باید low-cardinality باشند. نام کاربر، correlation id، نام فایل آزاد یا مقدار configuration نباید metric tag شود.
