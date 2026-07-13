# راهنمای فنی `scm-observation-starter`

## ۱. هدف و مرز ماژول

`scm-observation-starter` هستهٔ مشترک Observation است و چهار signal را پشتیبانی می‌کند:

```text
LOG
TRACE
AUDIT
METRIC
```

این ماژول مستقل از transport و مستقل از ماژول میزبان است. هسته نباید دربارهٔ HTTP، Servlet، Camel، SOAP، TCP، MQ، WebSocket، gRPC، دیتابیس تعریف سرویس یا ساختار یک application مشخص تصمیم بگیرد.

مسئولیت هسته:

- تعریف API تولید LOG، TRACE، AUDIT و METRIC؛
- مدیریت `ObservationScope` و lifecycle؛
- ساخت و اعتبارسنجی سند نهایی؛
- نگهداری `ObservationAttributeRegistry`؛
- type enforcement، sanitization و sensitivity policy؛
- تولید فایل‌های structured و اتصال Metric به Micrometer؛
- تولید metadata مشترک و target index؛
- فراهم‌کردن extension point برای attributeهای ماژول میزبان.

مسئولیت ماژول میزبان یا adapter:

- تعیین محل شروع و پایان span؛
- استخراج metadata پروتکل؛
- تعریف eventهای business یا technical؛
- register کردن attributeهای اختصاصی؛
- انتخاب policy فعال‌سازی signalها؛
- اتصال eventهای داخلی application به Observation؛
- جلوگیری از ارسال credential و payload غیرمجاز.

جهت dependency:

```text
host module -> scm-observation-starter
transport adapter -> scm-observation-starter
scm-observation-starter -X-> host module
scm-observation-starter -X-> transport adapter
```

## ۲. مسیر خروجی signalها

```text
LOG    -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
TRACE  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
AUDIT  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
METRIC -> Actuator -> Micrometer -> Prometheus -> Grafana
```

Metric در فایل LOG، TRACE یا AUDIT نوشته نمی‌شود.

Policy پیش‌فرض starter:

| Signal | پیش‌فرض |
| --- | --- |
| LOG | فعال |
| TRACE | فعال |
| AUDIT | غیرفعال |
| METRIC | غیرفعال |

فعال‌کردن console یا file، signal والد را فعال نمی‌کند. برای مثال `SCM_OBS_AUDIT_CONSOLE_ENABLED=true` بدون `SCM_OBS_AUDIT_ENABLED=true` باعث تولید Audit نمی‌شود.

## ۳. قرارداد تنظیمات بیرونی

### ۳.۱. اصل تنظیم با variable

قرارداد عملیاتی Observation بر پایهٔ environment variable است. propertyهای Spring فقط binding داخلی هستند و نباید در deployment مستقیماً تنظیم شوند.

الگوی نام‌گذاری:

```text
SCM_OBS_<SIGNAL>_<AREA>_<SETTING>
```

نمونه:

```text
SCM_OBS_TRACE_ENABLED
SCM_OBS_TRACE_FILE_FORMAT
SCM_OBS_TRACE_MAX_FILE_SIZE
SCM_OBS_AUDIT_ASYNC_QUEUE_SIZE
```

Variableهای هویتی مشترک خارج از prefix بالا هستند:

```text
SCM_APP
SCM_ENV
SCM_LABEL
SCM_METADATA_NAMESPACE
SCM_METADATA_INSTANCE_ID
SCM_METADATA_TIME_ZONE
```

هر میزبان باید variableهای مورد استفاده و مقدار نمونهٔ خودش را در مستند همان ماژول توضیح دهد.

### ۳.۲. اولویت منابع

از بیشترین تا کمترین اولویت:

1. command-line argument؛
2. system property؛
3. environment variable؛
4. Spring Cloud Config؛
5. profile configuration؛
6. host configuration؛
7. defaults متعلق به starter؛
8. fail-safe داخلی Java و Logback.

فایل `META-INF/scm/observation-defaults.yml` با کمترین اولویت بارگذاری می‌شود. میزبان نباید آن را با `spring.config.import` وارد کند.

### ۳.۳. هویت runtime

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_APP` | `payment-service` | نام application و مقدار `service.name` |
| `SCM_ENV` | `prod` | profile محیط؛ یکی از `dev`, `test`, `pilot`, `prod` |
| `SCM_LABEL` | `release-8.6` | label مربوط به Config Server |
| `SCM_METADATA_NAMESPACE` | `payments` | namespace استقرار |
| `SCM_METADATA_INSTANCE_ID` | `payment-service-7f9c` | شناسهٔ instance یا Pod |
| `SCM_METADATA_TIME_ZONE` | `Asia/Tehran` | timezone مربوط به metadata فایل؛ خالی یعنی timezone JVM |

نمونهٔ Kubernetes:

```yaml
env:
  - name: SCM_METADATA_NAMESPACE
    valueFrom:
      fieldRef:
        fieldPath: metadata.namespace
  - name: SCM_METADATA_INSTANCE_ID
    valueFrom:
      fieldRef:
        fieldPath: metadata.name
```

### ۳.۴. Variableهای عمومی

| Variable | Default | کاربرد |
| --- | --- | --- |
| `SCM_OBS_ENABLED` | `true` | کلید اصلی Observation |
| `SCM_OBS_ROOT_DIR` | `${user.home}/scm/obs` | ریشهٔ فایل‌ها |

### ۳.۵. Variableهای LOG

| Variable | Default |
| --- | --- |
| `SCM_OBS_LOG_ENABLED` | `true` |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | `false` |
| `SCM_OBS_LOG_CONSOLE_FORMAT` | `simple` |
| `SCM_OBS_LOG_FILE_ENABLED` | `true` |
| `SCM_OBS_LOG_FILE_FORMAT` | `jsonl` |
| `SCM_OBS_LOG_DIR` | مسیر مشتق‌شده از root و metadata |
| `SCM_OBS_LOG_MAX_FILE_SIZE` | `100MB` |
| `SCM_OBS_LOG_MAX_HISTORY` | `30` |
| `SCM_OBS_LOG_TOTAL_SIZE_CAP` | `10GB` |
| `SCM_OBS_LOG_CLEAN_HISTORY_ON_START` | `false` |

### ۳.۶. Variableهای TRACE

| Variable | Default |
| --- | --- |
| `SCM_OBS_TRACE_ENABLED` | `true` |
| `SCM_OBS_TRACE_CONSOLE_ENABLED` | `false` |
| `SCM_OBS_TRACE_CONSOLE_FORMAT` | `simple` |
| `SCM_OBS_TRACE_FILE_ENABLED` | `true` |
| `SCM_OBS_TRACE_FILE_FORMAT` | `jsonl` |
| `SCM_OBS_TRACE_DIR` | مسیر مشتق‌شده از root و metadata |
| `SCM_OBS_TRACE_MAX_FILE_SIZE` | `100MB` |
| `SCM_OBS_TRACE_MAX_HISTORY` | `30` |
| `SCM_OBS_TRACE_TOTAL_SIZE_CAP` | `10GB` |
| `SCM_OBS_TRACE_CLEAN_HISTORY_ON_START` | `false` |
| `SCM_OBS_TRACE_ASYNC_ENABLED` | `false` |
| `SCM_OBS_TRACE_ASYNC_QUEUE_SIZE` | `8192` |
| `SCM_OBS_TRACE_ASYNC_DISCARDING_THRESHOLD` | `0` |
| `SCM_OBS_TRACE_ASYNC_NEVER_BLOCK` | `false` |
| `SCM_OBS_TRACE_ASYNC_MAX_FLUSH_TIME` | `1000` |

### ۳.۷. Variableهای AUDIT

| Variable | Default |
| --- | --- |
| `SCM_OBS_AUDIT_ENABLED` | `false` |
| `SCM_OBS_AUDIT_CONSOLE_ENABLED` | `false` |
| `SCM_OBS_AUDIT_CONSOLE_FORMAT` | `simple` |
| `SCM_OBS_AUDIT_FILE_ENABLED` | `true` |
| `SCM_OBS_AUDIT_FILE_FORMAT` | `jsonl` |
| `SCM_OBS_AUDIT_DIR` | مسیر مشتق‌شده از root و metadata |
| `SCM_OBS_AUDIT_MAX_FILE_SIZE` | `100MB` |
| `SCM_OBS_AUDIT_MAX_HISTORY` | `90` |
| `SCM_OBS_AUDIT_TOTAL_SIZE_CAP` | `20GB` |
| `SCM_OBS_AUDIT_CLEAN_HISTORY_ON_START` | `false` |
| `SCM_OBS_AUDIT_ASYNC_ENABLED` | `false` |
| `SCM_OBS_AUDIT_ASYNC_QUEUE_SIZE` | `8192` |
| `SCM_OBS_AUDIT_ASYNC_DISCARDING_THRESHOLD` | `0` |
| `SCM_OBS_AUDIT_ASYNC_NEVER_BLOCK` | `false` |
| `SCM_OBS_AUDIT_ASYNC_MAX_FLUSH_TIME` | `1000` |

### ۳.۸. Variableهای METRIC

| Variable | Default |
| --- | --- |
| `SCM_OBS_METRIC_ENABLED` | `false` |

### ۳.۹. Format

مقادیر format فقط این دو مقدار lowercase را می‌پذیرند:

```text
simple
jsonl
```

مقدار نامعتبر، uppercase یا خالی باید startup را fail کند. `jsonl` قرارداد استاندارد ingestion است؛ `simple` برای مشاهدهٔ انسانی است.

## ۴. فایل و target index

نام فایل از application، environment، namespace و instance ساخته می‌شود و channel در نام فایل قرار نمی‌گیرد.

```text
{root}/{app}/{env}/{namespace}/{stream}/
```

نمونهٔ نام فعال TRACE:

```text
trace-scm-payment-service-prod-payments-payment-service-7f9c.jsonl
```

فایل rolled ساعت و index رول را اضافه می‌کند.

`scm.observation.target.index` مستقل از نام فایل است و برای routing در Elasticsearch ساخته می‌شود:

```text
{stream}-scm-{namespace}-{env}-{channelCode}-{yyyy.MM.dd.HH}
```

اگر channel معتبر وجود نداشته باشد، segment مربوط به channel حذف می‌شود.

## ۵. Attribute Registry

### ۵.۱. قانون اصلی

هر attribute قبل از استفاده باید در `ObservationAttributeRegistry` برای stream مربوطه ثبت شده باشد. attributeهای dynamic یا نام‌های آزاد پذیرفته نیستند.

ثبت attribute مشخص می‌کند:

- نام نهایی؛
- نوع Java و Elasticsearch؛
- owner؛
- streamهای مجاز؛
- presence؛
- sensitivity؛
- توضیح فنی.

این قانون از typo، تغییر type در اسناد مختلف و ایجاد mapping ناسازگار جلوگیری می‌کند.

### ۵.۲. Extension point میزبان

ماژول میزبان یک bean از نوع `ObservationAttributeContributor` ارائه می‌کند:

```java
@Component
public final class HostObservationAttributeContributor
        implements ObservationAttributeContributor {

    @Override
    public Collection<ObservationAttributeKey<?>> attributes() {
        return List.of(HostTraceAttributes.SERVICE_REFERENCE);
    }
}
```

تعریف attribute:

```java
public static final ObservationAttributeKey<String> SERVICE_REFERENCE =
        TraceAttribute.keyword(
                "host.service.reference",
                "host-module",
                ObservationAttributePresence.EVENT_OPTIONAL,
                "Stable service reference."
        );
```

نام تکراری با metadata ناسازگار startup را fail می‌کند.

### ۵.۳. Typeهای پایه

```text
KEYWORD
TEXT
DATE
LONG
INTEGER
DOUBLE
BOOLEAN
OBJECT
```

Producer نباید برای یک attribute در اسناد مختلف type متفاوت ارسال کند. registry منبع اصلی type است و مقدار قبل از خروجی باید به type ثبت‌شده تبدیل یا رد شود.

### ۵.۴. Sensitivity

```text
RAW
SECURE
MASK_PREFIX
MASK_SUFFIX
MASK_PREFIX_SUFFIX
```

`RAW` به معنی مجاز بودن خودکار هر داده‌ای نیست. ماژول مالک باید فقط داده‌ای را register کند که policy آن سامانه اجازه می‌دهد. credentialها و secretها حتی با وجود sanitizer نباید وارد API Observation شوند.

## ۶. مسیر افزودن attribute جدید

1. semantic و نام attribute را مشخص کنید.
2. owner و stream را مشخص کنید.
3. type Elasticsearch را انتخاب کنید.
4. sensitivity را تعیین کنید.
5. attribute را در کلاس attributeهای ماژول تعریف کنید.
6. آن را از طریق contributor در registry ثبت کنید.
7. producer یا adapter را به استفاده از همان key متصل کنید.
8. نمونهٔ خروجی و محل تولید را در مستند ماژول مالک اضافه کنید.
9. mapping و dashboard downstream را بررسی کنید.

تغییر تنها در configuration بدون registration معتبر نیست.

## ۷. مدل TRACE

### ۷.۱. شناسه‌ها

- `trace.id`: شناسهٔ distributed trace بین سرویس‌ها؛
- `span.id`: شناسهٔ span جاری؛
- `parent.span.id`: شناسهٔ span والد؛
- `correlation.id`: شناسهٔ محلی پردازش در یک application.

Distributed propagation فقط از W3C `traceparent` استفاده می‌کند. `correlation.id` بین microserviceها منتقل نمی‌شود.

### ۷.۲. زمان‌ها

هر span کامل باید داشته باشد:

```text
span.start_time
span.end_time
span.duration_ms
```

زمان‌های ابتدا و انتها UTC هستند و duration با elapsed time یکنواخت محاسبه می‌شود.

### ۷.۳. Span و span event

Span یک بازهٔ زمانی است. Span event یک رخداد نقطه‌ای داخل همان span است و child span ایجاد نمی‌کند.

نمونه:

```text
gateway.receive
  event: gateway.request.received
  event: plugin.execute
  event: gateway.response.completed
```

### ۷.۴. Detached scope

برای pipelineهای asynchronous، میزبان می‌تواند span را detached شروع کند و `ObservationScope` و `TraceContext` را در context محلی پیام نگه دارد. Scope نباید به‌صورت بلندمدت روی ThreadLocal یک thread pool باقی بماند.

قواعد:

- scope دقیقاً یک بار بسته شود؛
- context در پیام outbound serialize نشود؛
- processor فقط در مدت invocation خود context را bind کند؛
- cleanup در `finally` انجام شود.

## ۸. قرارداد adapter و میزبان

هسته transport-neutral است. هر adapter باید:

1. metadata امن پروتکل را استخراج کند؛
2. attributeهای موردنیاز را از registry دریافت کند؛
3. lifecycle را در نقطهٔ واقعی پردازش شروع و پایان دهد؛
4. دادهٔ transport را به semantic attribute تبدیل کند؛
5. از ثبت body، header یا credential بدون allowlist جلوگیری کند؛
6. خطای observation runtime را از business flow جدا نگه دارد.

Generic HTTP، Camel gateway، TCP، MQ و protocolهای دیگر باید adapter مستقل داشته باشند و هسته را به transport خود وابسته نکنند.

## ۹. قرارداد definition مبتنی بر configuration

یک میزبان می‌تواند برای enrichment از definition خارجی استفاده کند. محل ذخیره‌سازی definition، مدل سرویس و route lifecycle متعلق به میزبان است و در starter تعریف نمی‌شود.

قواعد عمومی:

- نبود definition مجاز است؛
- اگر definition وجود دارد، قبل از فعال‌شدن route یا processor validate شود؛
- تمام attributeها باید در registry ثبت شده باشند؛
- type ثبت‌شده منبع اصلی type است؛
- definition نامعتبر باید با context کافی fail-fast شود؛
- runtime extraction failure نباید business request را fail کند؛
- secret و attributeهای سیستمی قابل overwrite نیستند؛
- افزودن attribute آینده ابتدا registration و سپس configuration می‌خواهد.

Context خطای startup باید شامل اطلاعاتی مانند route، service، version، definition، rule، attribute، source و reason باشد.

## ۱۰. LOG، AUDIT و METRIC

### LOG

برای رخداد عملیاتی روزمره استفاده می‌شود. Structured LOG باید از attributeهای ثبت‌شده، MDC و sanitizer مشترک استفاده کند.

### AUDIT

برای رخداد قابل استناد business، security یا administration است و جایگزین LOG نیست. Audit signal به‌صورت پیش‌فرض غیرفعال است و میزبان باید آن را آگاهانه با variable فعال کند.

### METRIC

نام metric و tagها باید low-cardinality باشند. user، account، token، request id، correlation id و مقادیر آزاد نباید tag شوند.

## ۱۱. اطلاعات ممنوع

موارد زیر نباید به‌صورت خام وارد LOG، TRACE، AUDIT، METRIC، event یا MDC شوند:

```text
Authorization
Cookie
Set-Cookie
access token
refresh token
password
secret
API key
OTP
PIN
CVV
private key
raw credential
unrestricted request/response payload
```

هر allowlist باید محدود، قابل بازبینی و متعلق به ماژول مالک باشد.

## ۱۲. چک‌لیست اتصال یک میزبان جدید

- dependency هسته اضافه شده است؛
- `SCM_APP`, `SCM_ENV`, namespace و instance id تعیین شده‌اند؛
- signalها فقط با variable فعال شده‌اند؛
- مقصد console/file مشخص است؛
- attributeهای اختصاصی register شده‌اند؛
- span و event مالک مشخص دارند؛
- transport adapter در ماژول مناسب قرار دارد؛
- lifecycle در failure path نیز بسته می‌شود؛
- credential و payload غیرمجاز حذف شده‌اند؛
- fail-fast پیکربندی قابل فهم است؛
- مستند همان ماژول به‌روزرسانی شده است.
