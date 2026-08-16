# Observation در `scm-cache`

## ۱. نقش ماژول

`scm-cache` سرور Hazelcast SCM است. این ماژول فقط از هستهٔ transport-neutral زیر استفاده می‌کند:

```text
scm-observation-starter
```

این ماژول به `scm-observation-servlet-starter` وابسته نیست. endpointهای Actuator، `/livez` و `/readyz` business request محسوب نمی‌شوند و generic HTTP business span ایجاد نمی‌کنند.

## ۲. Signalهای مورد استفاده

| Signal | Policy |
| --- | --- |
| LOG | فعال برای lifecycle، bootstrap، health و خطاها |
| TRACE | فعال برای lifecycleهای داخلی در صورت وجود span واقعی |
| AUDIT | غیرفعال، مگر اینکه در آینده operation مدیریتی قابل استناد اضافه شود |
| METRIC | فعال |

Metric مسیر فایل ندارد و از Actuator/Micrometer صادر می‌شود.

## ۳. Variableهای مشترک

| Variable | نمونه | کاربرد |
| --- | --- | --- |
| `SCM_APP` | `scm-cache` | نام application |
| `SCM_ENV` | `dev` | محیط اجرا |
| `SCM_LABEL` | `master` | Config label |
| `SCM_METADATA_NAMESPACE` | `local` | namespace |
| `SCM_METADATA_INSTANCE_ID` | `local-scm-cache` | شناسه instance |
| `SCM_METADATA_TIME_ZONE` | `Asia/Tehran` | timezone metadata |
| `SCM_OBS_ENABLED` | `true` | کلید اصلی Observation |
| `SCM_OBS_ROOT_DIR` | `/mnt/observation` | ریشه فایل‌ها |

## ۴. Variableهای Observation

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_OBS_LOG_ENABLED` | `true` | structured LOG |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | `true` در dev | console LOG |
| `SCM_OBS_LOG_CONSOLE_FORMAT` | `jsonl` | فرمت console |
| `SCM_OBS_LOG_FILE_ENABLED` | `true` | فایل LOG |
| `SCM_OBS_TRACE_ENABLED` | `true` | TRACE داخلی |
| `SCM_OBS_TRACE_CONSOLE_ENABLED` | `true` در dev | console TRACE |
| `SCM_OBS_TRACE_FILE_ENABLED` | `true` | فایل TRACE |
| `SCM_OBS_AUDIT_ENABLED` | `false` | Audit به‌صورت پیش‌فرض استفاده نمی‌شود |
| `SCM_OBS_AUDIT_CONSOLE_ENABLED` | `true` در dev | فقط مقصد console؛ Audit را فعال نمی‌کند |
| `SCM_OBS_METRIC_ENABLED` | `true` | metricهای Cache |

`scm-cache` نباید variableهای `SCM_OBS_HTTP_SERVER_*` را برای ایجاد business trace تنظیم کند، چون Servlet observation adapter در این ماژول وجود ندارد.

## ۵. Variableهای level

| Variable | نمونه |
| --- | --- |
| `SCM_LOG_LEVEL_ROOT` | `INFO` |
| `SCM_LOG_LEVEL_APPLICATION` | `INFO` |
| `SCM_LOG_LEVEL_SPRING` | `INFO` |
| `SCM_LOG_LEVEL_HIBERNATE` | `WARN` |
| `SCM_LOG_LEVEL_HAZELCAST` | `INFO` |
| `SCM_LOG_LEVEL_HIKARI` | `INFO` |

## ۶. Variableهای health و risk مرتبط با Observation

| Variable | Default | کاربرد |
| --- | --- | --- |
| `SCM_CACHE_HEALTH_MIN_CLUSTER_SIZE` | `1` | حداقل cluster size برای readiness |
| `SCM_CACHE_ELEMENT_WARNING_RATIO` | `0.80` | آستانه warning عمومی |
| `SCM_CACHE_ELEMENT_CRITICAL_RATIO` | `0.90` | آستانه critical عمومی |
| `SCM_CACHE_USER_WARNING_RATIO` | `0.80` | override مربوط به `user_cache` |
| `SCM_CACHE_USER_CRITICAL_RATIO` | `0.90` | override مربوط به `user_cache` |

مقادیر ratio باید بین صفر و یک باشند و در startup اعتبارسنجی شوند.

## ۷. نمونهٔ local

```bash
export SCM_APP=scm-cache
export SCM_ENV=dev
export SCM_METADATA_NAMESPACE=local
export SCM_METADATA_INSTANCE_ID=local-scm-cache

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=false
export SCM_OBS_METRIC_ENABLED=true

export SCM_OBS_LOG_CONSOLE_ENABLED=true
export SCM_OBS_TRACE_CONSOLE_ENABLED=true
export SCM_OBS_LOG_CONSOLE_FORMAT=jsonl
export SCM_OBS_TRACE_CONSOLE_FORMAT=jsonl

export SCM_CACHE_HEALTH_MIN_CLUSTER_SIZE=1
export SCM_CACHE_ELEMENT_WARNING_RATIO=0.80
export SCM_CACHE_ELEMENT_CRITICAL_RATIO=0.90
```

## ۸. نمونهٔ production

```bash
export SCM_APP=scm-cache
export SCM_ENV=prod
export SCM_METADATA_NAMESPACE=scm-platform
export SCM_METADATA_INSTANCE_ID=scm-cache-0
export SCM_OBS_ROOT_DIR=/mnt/observation

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=false
export SCM_OBS_METRIC_ENABLED=true

export SCM_OBS_LOG_CONSOLE_ENABLED=false
export SCM_OBS_TRACE_CONSOLE_ENABLED=false
export SCM_OBS_LOG_FILE_ENABLED=true
export SCM_OBS_TRACE_FILE_ENABLED=true
```

## ۹. Structured LOG

دسته‌های پایدار:

```text
scm.cache.context
scm.cache.init
scm.cache.health
```

Actionهای پایدار:

```text
runtime.context.created
scm.cache.init.started
hazelcast.bootstrap.started
hazelcast.bootstrap.config.loaded
hazelcast.bootstrap.config.registered
hazelcast.bootstrap.member.started
hazelcast.bootstrap.objects.materialized
hazelcast.bootstrap.completed
hazelcast.bootstrap.failed
hazelcast.element.registered
hazelcast.element.materialized
hazelcast.health.changed
```

Logهای element فقط metadata محدود و ثبت‌شده را نگه می‌دارند:

```text
cache.hazelcast.element.type
cache.hazelcast.element.name
cache.hazelcast.element.config
```

`cache.hazelcast.element.config` فقط خلاصهٔ deterministic از گزینه‌های امن است. کل config Hazelcast، cache key، cache value، token، OTP، password یا payload نباید ثبت شود.

## ۱۰. Metric

Metricهای اصلی:

```text
scm.cache.hazelcast.cluster.size
scm.cache.hazelcast.member.running
scm.cache.hazelcast.bootstrap.completed
scm.cache.hazelcast.elements.registered
scm.cache.hazelcast.elements.materialized
scm.cache.hazelcast.elements.registered.by.type
scm.cache.hazelcast.elements.materialized.by.type
scm.element.health
scm.element.risk
scm.element.materialized
scm.element.capacity.ratio
```

Tagهای مجاز:

```text
service=scm-cache
component=hazelcast
element_type=<HazelcastElementType>
element_name=<finite configured element name>
```

`element_name` فقط برای مجموعهٔ محدود elementهای پیکربندی‌شده مجاز است. request، user، account، token و correlation id نباید metric tag شوند.

معنای risk:

```text
0 = normal
1 = warning
2 = critical
```

معنای health:

```text
1 = healthy
0 = unhealthy
```

## ۱۱. مسیر توسعه

برای افزودن log attribute یا metric جدید:

1. مالکیت field را در `scm-cache` مشخص کنید؛
2. attribute را با contributor ماژول register کنید؛
3. فقط metadata کم‌حجم و امن تولید کنید؛
4. metric tag را از نظر cardinality بررسی کنید؛
5. health/risk را در application محاسبه کنید، نه در dashboard؛
6. variable جدید را فقط برای policy قابل تنظیم اضافه کنید؛
7. همین سند و dashboardهای مرتبط را به‌روزرسانی کنید.

برای افزودن operation مدیریتی قابل استناد، ابتدا مدل Audit و eventهای آن باید طراحی شود؛ صرفاً فعال‌کردن `SCM_OBS_AUDIT_ENABLED` event جدید تولید نمی‌کند.
