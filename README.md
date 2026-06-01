# SCM Observability - Decentralized Documentation

## هدف

این README مستند ریشه برای طراحی و نگهداری زیرساخت Observability در پروژه SCM است.

اصل اصلی این طراحی این است که مستندات فنی هر ماژول باید داخل همان ماژول نگهداری شود و از ایجاد مستندات متمرکز و بزرگ در یک فولدر مشترک، مانند `docs/`، خودداری شود.

برنچ مبنا:

```text
features/CMNEW-119
```

---

## تصمیم معماری نهایی

معماری Observability در SCM به صورت Hybrid طراحی می‌شود:

```text
Trace  -> File NDJSON -> Filebeat -> Elasticsearch -> Kibana
Log    -> File NDJSON -> Filebeat -> Elasticsearch -> Kibana
Audit  -> File NDJSON -> Filebeat -> Elasticsearch -> Kibana

Metric -> Spring Boot Actuator -> Micrometer -> Prometheus -> Grafana
```

---

## توضیح تصمیم‌ها

### Trace

Trace در برنامه با OpenTelemetry تولید و enrich می‌شود، اما برای مشاهده و جست‌وجو در Kibana، رویدادهای Trace به صورت فایل‌های NDJSON ذخیره می‌شوند و سپس توسط Filebeat به Elasticsearch ارسال می‌شوند.

مسیر پیشنهادی:

```text
Application
    ↓
OpenTelemetry instrumentation
    ↓
trace.ndjson
    ↓
Filebeat
    ↓
Elasticsearch
    ↓
Kibana
```

---

### Log

Log با `logback-spring` تولید می‌شود و خروجی آن باید ترجیحاً JSON line / NDJSON باشد تا Filebeat بتواند بدون multiline پیچیده آن را بخواند.

مسیر پیشنهادی:

```text
Application
    ↓
logback-spring
    ↓
application.ndjson
    ↓
Filebeat
    ↓
Elasticsearch
    ↓
Kibana
```

---

### Audit

Audit از Log جدا است و برای رخدادهای امنیتی، مدیریتی و کسب‌وکاری قابل استناد استفاده می‌شود.

مسیر پیشنهادی فاز فعلی:

```text
Application
    ↓
ScmAuditPublisher
    ↓
audit.ndjson
    ↓
Filebeat
    ↓
Elasticsearch
    ↓
Kibana
```

در صورت نیاز آینده به compliance قوی‌تر، می‌توان ماژول مستقل `scm-audit-service` را برای ذخیره append-only در پایگاه داده یا storage رسمی اضافه کرد.

---

### Metric

Metric استثنا است و نباید در فایل ذخیره شود.

مسیر Metric باید همان روال استاندارد Spring Boot Actuator و Micrometer باقی بماند:

```text
Spring Boot Actuator
    ↓
Micrometer
    ↓
/actuator/prometheus
    ↓
Prometheus
    ↓
Grafana
```

قواعد Metric:

- Metric در فایل ذخیره نمی‌شود.
- Metric توسط Filebeat خوانده نمی‌شود.
- روال Actuator تغییر نمی‌کند.
- endpoint زیر حفظ می‌شود:

```text
/actuator/prometheus
```

---

## ساختار مستندات

مستندات نباید در فولدر متمرکز مثل `docs/` جمع شوند.

هر ماژول باید مستندات مربوط به خودش را در فولدر خودش داشته باشد.

ساختار پیشنهادی:

```text
README.md
AGENTS.md

scm-observability-api/
    README.md
    AGENTS.md

scm-observability-file-sink/
    README.md
    AGENTS.md

scm-observability-spring-boot-starter/
    README.md
    AGENTS.md

scm-audit-api/
    README.md
    AGENTS.md

scm-audit-client/
    README.md
    AGENTS.md

scm-uaa/
    OBSERVABILITY.md

scm-web/
    OBSERVABILITY.md

scm-gateway/
    OBSERVABILITY.md

scm-core/
    OBSERVABILITY.md

scm-logging/
    OBSERVABILITY.md

scm-config/
    OBSERVABILITY.md

scm-provider-rest/
    OBSERVABILITY.md

scm-provider-shetab/
    OBSERVABILITY.md

filebeat/
    README.md
```

---

## اصل مهم مستندسازی

اگر تغییری مربوط به یک ماژول است، مستند همان تغییر باید داخل همان ماژول به‌روزرسانی شود.

نمونه:

| نوع تغییر | محل مستند |
|---|---|
| تغییر در UAA Trace/Login | `scm-uaa/OBSERVABILITY.md` |
| تغییر در Provider REST observation | `scm-provider-rest/OBSERVABILITY.md` |
| تغییر در Filebeat input | `filebeat/README.md` |
| تغییر در Audit API | `scm-audit-api/README.md` |
| تغییر در Audit publisher | `scm-audit-client/README.md` |
| تغییر در File Sink | `scm-observability-file-sink/README.md` |
| تغییر در قواعد کلی معماری | همین `README.md` ریشه |

---

## ماژول‌های پیشنهادی Observability

### `scm-observability-api`

ماژول سبک و مستقل برای قراردادها و constantهای مشترک.

مسئولیت‌ها:

```text
ScmTraceAttributes
ScmMdcKeys
ScmMetricNames
ScmObservationType
ScmObservationResult
ScmSensitiveFields
```

نکته مهم:

`ScmMetricNames` فقط نام metricها را نگه می‌دارد. Metric در فایل ذخیره نمی‌شود.

---

### `scm-observability-file-sink`

ماژول نوشتن رویدادهای Observability در فایل NDJSON.

دامنه این ماژول فقط موارد زیر است:

```text
Trace
Application Log Event
Audit
```

این ماژول نباید Metric بنویسد.

---

### `scm-observability-spring-boot-starter`

ماژول AutoConfiguration برای استفاده راحت در ماژول‌های Spring Boot.

مسئولیت‌ها:

```text
CorrelationIdFilter
MdcFilter
Trace file sink auto-configuration
Audit publisher auto-configuration
Log sanitization support
Actuator/Micrometer/Prometheus support
```

---

### `scm-audit-api`

ماژول قراردادها و مدل‌های Audit.

مسئولیت‌ها:

```text
ScmAuditEvent
ScmAuditCategory
ScmAuditAction
ScmAuditResult
```

---

### `scm-audit-client`

ماژول انتشار Audit Event از ماژول‌های application.

در فاز فعلی:

```text
ScmAuditPublisher -> audit.ndjson
```

در آینده، در صورت نیاز compliance:

```text
ScmAuditPublisher -> scm-audit-service
```

---

## اطلاعات ممنوع در Trace / Log / Audit / Metric

موارد زیر نباید در Trace، Log، Audit یا Metric ثبت شوند:

```text
password
client_secret
access_token
refresh_token
authorization_code
pin
cvv2
pan کامل کارت
track data
token
credential
username خام
account number
```

برای actor یا user، در صورت نیاز از hash یا شناسه pseudonymized استفاده شود.

---

## استاندارد پیام Commit

در این پروژه پیام‌های commit باید خوانا، قابل ردیابی و قابل استفاده برای Jira، CI/CD و ابزارهای تولید changelog باشند.

از قالبی نزدیک به Conventional Commits استفاده می‌کنیم، با این تفاوت که شماره تسک Jira باید در ابتدای خط اول قرار بگیرد.

قالب استاندارد:

```text
<JIRA-KEY> <type>(<scope>): <subject>
```

نمونه:

```text
CMNEW-119 feat(uaa): record authentication result on span
```

---

## اجزای پیام Commit

| بخش | توضیح |
|---|---|
| `JIRA-KEY` | شماره تسک Jira، مثل `CMNEW-119` |
| `type` | نوع تغییر |
| `scope` | ماژول یا قابلیت تحت تغییر |
| `subject` | توضیح کوتاه، شفاف و امری درباره تغییر |

---

## نوع‌های مجاز Commit

| Type | کاربرد |
|---|---|
| `feat` | افزودن قابلیت جدید |
| `fix` | رفع باگ |
| `refactor` | بازآرایی کد بدون تغییر رفتار |
| `docs` | تغییر یا افزودن مستندات |
| `test` | افزودن یا اصلاح تست |
| `build` | تغییرات Gradle، dependency یا build script |
| `ci` | تغییرات pipeline و CI/CD |
| `chore` | کارهای نگهداری عمومی |
| `perf` | بهبود performance |
| `style` | تغییرات formatting بدون تغییر منطق |
| `revert` | برگرداندن commit قبلی |

---

## Scopeهای پیشنهادی

Scope بهتر است نام ماژول یا قابلیت باشد.

```text
uaa
web
gateway
core
config
logging
cache
audit
observability
provider-rest
provider-shetab
plugin
filebeat
build
ci
docs
```

---

## قواعد نوشتن Subject

- کوتاه و شفاف باشد.
- انگلیسی نوشته شود.
- با فعل امری نوشته شود.
- در انتهای subject نقطه گذاشته نشود.
- فقط درباره همان تغییر commit صحبت کند.
- هر commit فقط یک هدف مشخص داشته باشد.

نمونه‌های خوب:

```text
CMNEW-119 docs(observability): document hybrid observability architecture
CMNEW-119 feat(audit): add audit event contract
CMNEW-119 feat(uaa): publish audit event for login failure
CMNEW-119 build(root): include observability modules
CMNEW-119 fix(logging): prevent duplicate audit event ingestion
```

---

## Body در Commit Message

اگر تغییر نیاز به توضیح بیشتر دارد، بعد از خط اول یک خط خالی گذاشته شود و سپس توضیح داده شود که چه چیزی تغییر کرده و چرا.

نمونه:

```text
CMNEW-119 docs(observability): document decentralized hybrid observability

Document the SCM observability architecture with decentralized module-level docs.
Define Trace, Log and Audit as NDJSON file-first flows through Filebeat, Elasticsearch and Kibana.
Keep Metrics on the standard Spring Boot Actuator, Micrometer, Prometheus and Grafana path.
```

---

## Breaking Change

اگر تغییر با نسخه قبلی ناسازگار است، از `!` بعد از scope استفاده شود و در body توضیح داده شود.

نمونه:

```text
CMNEW-119 feat(audit)!: change audit event schema

Require eventId and occurredAt for every audit event.

BREAKING CHANGE: audit event schema is not backward compatible.
```

---

## Commit Message پیشنهادی برای این تغییر

برای اضافه شدن این مستندات، پیام commit پیشنهادی:

```text
CMNEW-119 docs(observability): document decentralized hybrid observability

Document the SCM observability architecture with decentralized module-level docs.
Define Trace, Log and Audit as NDJSON file-first flows through Filebeat, Elasticsearch and Kibana.
Keep Metrics on the standard Spring Boot Actuator, Micrometer, Prometheus and Grafana path.
```

---

## جمع‌بندی

قواعد اصلی این طراحی:

```text
Trace  -> File -> Filebeat -> Elasticsearch -> Kibana
Log    -> File -> Filebeat -> Elasticsearch -> Kibana
Audit  -> File -> Filebeat -> Elasticsearch -> Kibana
Metric -> Actuator -> Prometheus -> Grafana
```

مستندات هر ماژول باید داخل همان ماژول نگهداری شود و root فقط تصمیم‌های کلی، قواعد مشترک و استانداردهای تیمی را توضیح دهد.
