# راهنمای `scm-observation-starter`

## ۱. هدف ماژول

`scm-observation-starter` زیرساخت مشترک مشاهده‌پذیری SCM برای چهار سیگنال اصلی است:

- `LOG`: ثبت رخدادهای ساخت‌یافته و قابل جست‌وجو
- `TRACE`: نمایش مسیر اجرا، زمان‌بندی و رابطهٔ والد/فرزند
- `AUDIT`: ثبت رویدادهای ممیزی، مستقل از Application Log
- `METRIC`: تولید شاخص‌های عددی از مسیر استاندارد Actuator و Micrometer

معماری خروجی:

```text
LOG    -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
TRACE  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
AUDIT  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
METRIC -> Actuator -> Micrometer -> Prometheus -> Grafana
```

Technical defaults are owned by this starter and loaded from
`META-INF/scm/observation-defaults.yml` as a lowest-precedence property source. Host applications contain only service policy, profile behavior, service-specific HTTP settings, and real exceptional overrides.

The default signal policy is:

| Signal | Enabled |
| --- | --- |
| LOG | `true` |
| TRACE | `true` |
| AUDIT | `false` |
| METRIC | `false` |

Destination defaults are:

| Stream | Console enabled | Console format | File enabled | File format |
| --- | --- | --- | --- | --- |
| LOG | `false` | `simple` | `true` | `jsonl` |
| TRACE | `false` | `simple` | `true` | `jsonl` |
| AUDIT | `false` | `simple` | `true` | `jsonl` |

`scm.observation.enabled` is the global switch. When it is `false`, the starter creates no LOG, TRACE, AUDIT, or METRIC signal and no SCM observation console or file appender emits output. Enabling a destination never enables its parent signal; in particular, enabling the AUDIT console does not enable AUDIT.

The effective precedence, from highest to lowest, is:

1. command-line argument
2. system property
3. OS environment variable
4. Spring Cloud Config
5. external or profile-specific application configuration
6. host application configuration
7. starter observation defaults
8. hard fail-safe Java and Logback defaults

The defaults are not imported with `spring.config.import`. The starter loads them after normal Config Data processing and adds them at lowest precedence, so every host and runtime source can override them. If that resource cannot be loaded, hard fail-safe enablement defaults remain `false`.

The six `scm.observation.{log,trace,audit}.{console,file}.format` properties accept exactly lowercase, case-sensitive `simple` or `jsonl`. Blank, uppercase, comma-separated, or other values fail startup even when the destination is disabled. `jsonl` is the default structured file-ingestion contract. `simple` is optional human-readable output and is not automatically compatible with the JSONL Filebeat pipeline.

A compact host policy override is sufficient:

```yaml
scm:
  observation:
    audit:
      enabled: true
    http:
      server:
        span-name: uaa.http.request
```

The `dev` profile enables destinations, not the AUDIT signal:

```yaml
scm:
  observation:
    log:
      console:
        enabled: true
    trace:
      console:
        enabled: true
    audit:
      console:
        enabled: true
```

Metric در فایل نوشته نمی‌شود و همان مسیر استاندارد Actuator، Micrometer، Prometheus و Grafana را طی می‌کند. مدل Audit نیز از Application Log جدا باقی می‌ماند.

## ۲. مرزبندی مسئولیت‌ها

مرز ماژول‌ها باید به شکل زیر حفظ شود:

```text
scm-observation-starter:
  ابزار عمومی log / trace / metric / audit

scm-uaa-starter:
  فقط security و resource-server
  بدون dependency به scm-observation-starter
  فقط انتشار event خنثی

scm-web:
  ماژول میزبان
  مالک اتصال security / provider / plugin / gateway به observation
```

`scm-uaa-starter` نباید به `scm-observation-starter` وابسته باشد و نباید `ScmObservation`، `ObservationScope` یا attributeهای observation را بشناسد. این starter فقط رفتار resource-server را پیاده‌سازی و رخدادهای امنیتی خنثی را منتشر می‌کند.

Providerها و starterهای دیگر نیز نباید برای ثبت log معمولی مجبور به dependency روی `scm-observation-starter` شوند. آن‌ها می‌توانند از `SLF4J` و `@Slf4j` استفاده کنند و برای رخدادهای رسمی SCM، event منتشر کنند. `scm-web` که هر دو سمت را در اختیار دارد، eventها را به observation تبدیل می‌کند.

جهت dependency مجاز:

```text
scm-web -> scm-uaa-starter
scm-web -> scm-observation-starter
```

جهت dependency غیرمجاز:

```text
scm-uaa-starter -> scm-observation-starter
provider starter -> scm-observation-starter
security starter -> scm-observation-starter
```

## ۳. تفاوت log معمولی، event، observation log، trace span و span event

### ۳.۱. log معمولی با SLF4J

log معمولی با `log.info`، `log.warn` و `log.error` تولید می‌شود. تنظیمات `Logback` در ماژول میزبان تعیین می‌کند خروجی کجا و با چه قالبی نوشته شود. همهٔ ماژول‌ها می‌توانند از این روش استفاده کنند و فیلدهای `MDC` مانند `correlationId`، `traceId` و `spanId` نیز در الگوی log نمایش داده شوند.

این روش برای پیام‌های عملیاتی روزمره، مانند شروع اتصال، retry، انتخاب endpoint یا خطای پیکربندی مناسب است.

### ۳.۲. SCM application event

SCM application event با `ScmEventPublisher` منتشر می‌شود و در محیط Spring بر `ApplicationEventPublisher` تکیه دارد. انتشار به‌صورت پیش‌فرض synchronous و داخل همان JVM است.

هدف event، جداکردن starter و provider از observation است. منتشرکننده فقط یک رخداد امن و خنثی تولید می‌کند و دربارهٔ log، trace یا metric تصمیم نمی‌گیرد.

### ۳.۳. Observation log

Observation log یک رکورد ساخت‌یافته است که از طریق `ScmObservation` نوشته می‌شود. این رکورد:

- متعلق به ماژول میزبان است؛
- با `ObservationAttributeRegistry` اعتبارسنجی می‌شود؛
- قبل از خروجی sanitize و mask می‌شود؛
- برای جست‌وجوی پایدار در Elasticsearch طراحی شده است.

### ۳.۴. Trace span

Span یک بازهٔ زمانی از اجرای برنامه را نمایش می‌دهد. هر span دارای `trace.id`، `span.id` و در صورت وجود والد، `parent.span.id` است. نمونه‌های اصلی SCM:

```text
gateway.receive
service.execute
operation.call
```

### ۳.۵. Span event

Span event یک رخداد نقطه‌ای داخل span جاری است و span مستقل جدید ایجاد نمی‌کند. برای رویدادهایی مانند اجرای plugin یا دریافت پاسخ provider مناسب است:

```text
plugin.before.started
plugin.before.completed
provider.response.received
```

ساختار پایدار span event در trace:

```json
{
  "span.events": [
    {
      "name": "plugin.before.completed",
      "timestamp": "2026-07-05T10:15:30Z",
      "attributes": {
        "plugin.name": "requestTransformer",
        "plugin.outcome": "success"
      }
    }
  ]
}
```

## ۴. چرا event bus مشترک داریم؟

پروژه یک abstraction کوچک و مشترک برای event دارد:

```text
ScmEventPublisher
ScmEvent
ScmEventType
```

پیاده‌سازی Spring آن از `ApplicationEventPublisher` استفاده می‌کند. این انتخاب با الگوی event/listener موجود در بخش‌های دیگر پروژه، از جمله جریان‌های audit، هماهنگ است و به Kafka، RabbitMQ، broker یا outbox نیاز ندارد.

Eventها به‌صورت پیش‌فرض synchronous هستند تا context همان thread حفظ شود:

- thread context
- `MDC`
- trace context

`ScmEventPublisher` باید سبک باشد و خطای listener نباید جریان business را متوقف کند، مگر آن‌که یک جریان مشخص صریحاً رفتار دیگری تعریف کرده باشد. Event payload نیز باید تا حد امکان immutable و همیشه فاقد secret باشد.

## ۵. معماری security event

`scm-uaa-starter` رخدادهای امنیتی خنثی زیر را منتشر می‌کند:

```text
AUTHENTICATION_STARTED
AUTHENTICATION_SUCCESS
AUTHENTICATION_FAILURE
ACCESS_DENIED
TOKEN_MISSING
TOKEN_INVALID
TOKEN_EXPIRED
```

این starter observation log یا trace را مستقیماً نمی‌نویسد. در نتیجه security starter مستقل می‌ماند و hostهای مختلف می‌توانند سیاست observation متفاوتی داشته باشند.

`scm-web` به این eventها گوش می‌دهد و بر اساس تنظیمات میزبان آن‌ها را به موارد زیر تبدیل می‌کند:

- structured observation log؛
- trace event روی span جاری؛
- metric در صورت نیاز.

Attributeهای security event فقط باید اطلاعات امنی مانند `http.method`، `url.path`، `client.ip`، شناسه‌های غیرحساس کاربر و error code امن را حمل کنند. JWT یا headerهای احراز هویت نباید داخل event قرار گیرند.

## ۶. قانون `correlation.id`

`correlation.id` یک شناسهٔ محلی برای هر microservice است:

- در ابتدای پردازش request یا message محلی ساخته می‌شود؛
- در تمام پردازش همان microservice ثابت می‌ماند؛
- با کلید `correlationId` در `MDC` قرار می‌گیرد؛
- به microservice پایین‌دست ارسال نمی‌شود؛
- هر microservice برای پردازش محلی خود شناسهٔ جدید می‌سازد.

نمونه:

```text
scm-web:
  correlation.id = C1

scm-uaa:
  correlation.id = C2

هر دو ممکن است در یک trace باشند، اما correlation جدا دارند.
```

این تفکیک باعث می‌شود `correlation.id` برای جست‌وجوی logهای محلی قابل اتکا باشد و هم‌زمان مرز پردازش هر سرویس را مشخص کند.

## ۷. قانون distributed trace و `traceparent`

انتقال distributed trace فقط با استاندارد W3C و header زیر انجام می‌شود:

```text
traceparent
```

Headerهای زیر نباید به downstream منتقل شوند:

```text
X-Correlation-ID
X-SCM-Trace-ID
X-SCM-Span-ID
X-SCM-Parent-Span-ID
```

قالب `traceparent`:

```text
traceparent: 00-<trace-id>-<current-span-id>-01
```

- `<trace-id>` شناسهٔ distributed trace است.
- `<current-span-id>` شناسهٔ span جاری در caller است.
- سرویس دریافت‌کننده این مقدار را به‌عنوان `parent.span.id` استفاده می‌کند.
- سرویس دریافت‌کننده یک `span.id` محلی جدید می‌سازد.
- سرویس دریافت‌کننده یک `correlation.id` محلی جدید می‌سازد.

نمونه:

```text
scm-web:
  trace.id = T1
  current span.id = S3

ارسال به scm-uaa:
  traceparent: 00-T1-S3-01

scm-uaa:
  trace.id = T1
  parent.span.id = S3
  span.id = S4
  correlation.id = C2
```

اگر `traceparent` ورودی وجود نداشته باشد، سرویس دریافت‌کننده یک `trace.id` جدید ایجاد می‌کند. اگر وجود داشته باشد، `trace.id` ورودی حفظ می‌شود، span مربوط به caller والد اولین span محلی خواهد بود و span محلی شناسهٔ جدید می‌گیرد.

## ۸. ساختار spanهای اصلی

ساختار منطقی اجرای business در `scm-web`:

```text
gateway.receive
  -> service.execute
      -> operation.call
```

`trace.id` در هر سه span ثابت است. هر span یک `span.id` مستقل دارد و `parent.span.id` ارتباط آن‌ها را مشخص می‌کند. `correlation.id` نیز در محدودهٔ پردازش محلی `scm-web` ثابت می‌ماند.

### `gateway.receive`

- root span مربوط به business request در `scm-web` است.
- protocol-neutral است و می‌تواند REST، SOAP، TCP، ISO، JMS یا protocolهای آینده را نمایش دهد.
- در ورودی gateway یا route شروع می‌شود، نه در یک filter صرفاً HTTP.
- پس از مشخص‌شدن نتیجهٔ نهایی business بسته می‌شود.
- تنها span مجاز برای projection به legacy DB است.
- نتیجهٔ نهایی موردنیاز legacy را جمع‌بندی می‌کند، اما payload کامل provider را نگه نمی‌دارد.

### `service.execute`

- فرزند `gateway.receive` است.
- هنگام شروع اجرای SCM service باز می‌شود.
- پس از مشخص‌شدن نتیجه یا failure سرویس بسته می‌شود.
- برای زمان‌بندی و عیب‌یابی لایهٔ service استفاده می‌شود.

### `operation.call`

- فرزند `service.execute` است.
- اطراف provider call، downstream microservice call یا اجرای operation باز می‌شود.
- پس از دریافت نتیجه یا failure پایین‌دست بسته می‌شود.
- برای call خارجی معمولاً `span.kind=client` دارد.

## ۹. SCM runtime metadata, routing, and legacy projection

Every LOG, TRACE, and AUDIT document must include these common routing and runtime metadata fields:

```text
event.stream
scm.metadata.namespace
scm.metadata.instance_id
scm.metadata.time_zone
scm.config.label
scm.observation.target.index
service.name
deployment.environment
```

SCM deployment variables map to Spring properties as follows:

```text
SCM_APP                  -> spring.application.name
SCM_ENV                  -> spring.profiles.active
SCM_LABEL                -> spring.cloud.config.label
SCM_LABEL                -> spring.cloud.config.server.git.default-label
SCM_METADATA_NAMESPACE   -> scm.metadata.namespace
SCM_METADATA_INSTANCE_ID -> scm.metadata.instance-id
SCM_METADATA_TIME_ZONE   -> scm.metadata.time-zone
```

`SCM_ENV` must be exactly one of `dev`, `test`, `pilot`, or `prod`. `SCM_LABEL` must be a single nonblank Config label. `scm.metadata.time-zone` is optional; blank values use the JVM system timezone for physical file naming metadata.

Kubernetes deployments provide namespace and instance id through the Downward API:

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

`scm.observation.target.index` is the final Elasticsearch routing index. The resolver is starter-owned, uses UTC for the hour bucket, and is not configurable by host applications. When a real business channel code exists, the pattern is:

```text
{stream}-scm-{namespace}-{env}-{channelCode}-{yyyy.MM.dd.HH}
```

When the channel code is missing, blank, `unknown`, `default`, `none`, `n/a`, or `n-a`, the channel part is omitted:

```text
{stream}-scm-{namespace}-{env}-{yyyy.MM.dd.HH}
```

Examples:

```text
log-scm-payment-prod-mb-2026.07.07.19
trace-scm-payment-prod-mb-2026.07.07.19
audit-scm-shared-prod-2026.07.07.19
```

Physical file routing is separate from Elasticsearch routing. The starter owns the default root directory, base-name pattern, and stream-directory derivation. Hosts override them only for an actual deployment requirement. The defaults are:

```text
root directory:    ${SCM_OBS_ROOT_DIR:${user.home}/scm/obs}
base-name pattern: scm-${spring.application.name}-${spring.profiles.active}-${scm.metadata.namespace}-${scm.metadata.instance-id}
```

`scm.observation.file.root-directory` controls the shared physical root directory. The supported `SCM_OBS_LOG_DIR`, `SCM_OBS_TRACE_DIR`, and `SCM_OBS_AUDIT_DIR` variables remain optional per-stream directory overrides. Directory resolution is:

```text
stream-specific directory override -> scm.observation.file.root-directory -> ${user.home}/scm/obs
```

`scm.observation.file.base-name-pattern` controls only the stable identity part of the filename. The starter appends stream, hour, roll index, and a format-specific extension:

```text
simple: {stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}-{rollIndex}.log
jsonl:  {stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}-{rollIndex}.jsonl
```

Files are under `{root}/{appName}/{env}/{namespace}/{stream}/`. The root directory is part of the physical path, not the filename. File names never include `scm.channel.code` or the Config label. Both patterns retain the hour token and roll index. Observation files are not gzipped and do not use a separate archive directory.

Every simple line is UTF-8, single-line `key=value` output and contains exactly one canonical stream identity: `stream=log`, `stream=trace`, or `stream=audit`. Text values are safely quoted and escaped; null values are omitted. Simple TRACE and AUDIT output is not JSON with a stream prefix.

`scm.channel.code` is a business attribute only. It may affect the target index when it is a real business channel code such as `ib` or `mb`, but it must not be used for physical file naming.

When `scm.observation.legacy.enabled=true`, `scm.observation.legacy.service.code` and `scm.observation.legacy.operation.code` must be present. Validation fails clearly if they are missing. Legacy service codes must come from explicit attributes or configured mappings; the starter does not parse `span.name` to produce legacy DB values.

Host modules apply legacy projection at their business entry points only:

- `scm-web`: the real end-user gateway entry span is `gateway.receive`, and its legacy service code comes from route/service configuration.
- `scm-uaa`: entry spans for `login`, `change-password`, `update-favorite-account`, and `update-account-label` may set legacy projection using configured service codes.
- `scm-cache`: default behavior is `scm.observation.legacy.enabled=false`.
- `scm-cm-connector`: provider/technical spans are not legacy by default; a root span can be legacy only when CM connector is the direct business entry point and configured service/operation codes are supplied.

Console output for LOG, TRACE, and AUDIT is enabled only in the `dev` profile. Enabling the AUDIT console does not change the default `audit.enabled=false` policy. Starter defaults keep consoles disabled and files enabled in other profiles. `clean-history-on-start=false` remains the default; current and rolled JSONL files keep final names while Filebeat reads them. Optional simple `.log` files are for human use and are not part of that ingestion contract.

Generic HTTP server tracing is disabled by default:

```yaml
scm:
  observation:
    http:
      server:
        enabled: false
        mode: channel-only
```

Business TRACE should be created only for real end-user channel calls, not actuator, internal, admin/config, static, documentation, or background job endpoints.

Distributed trace propagation uses the standard W3C `traceparent` header as the source of truth. Custom headers such as `X-SCM-Trace-ID`, `X-SCM-Span-ID`, and `X-SCM-Parent-Span-ID` are not distributed trace propagation sources.

## ۱۰. قواعد Provider

Providerها می‌توانند برای log عملیاتی معمولی از `@Slf4j` استفاده کنند. برای observation رسمی باید eventهای زیر را از طریق `ScmEventPublisher` منتشر کنند:

```text
PROVIDER_REQUEST_SENT
PROVIDER_RESPONSE_RECEIVED
PROVIDER_CALL_FAILED
PROVIDER_TIMEOUT
```

Trace مربوط به provider فقط metadata، زمان‌بندی و نتیجه را نگه می‌دارد:

```text
scm.provider.code
scm.provider.type
scm.provider.address
scm.provider.endpoint
scm.provider.request_time
scm.provider.response_time
scm.provider.duration_ms
scm.provider.result
scm.provider.response_code
http.status_code
```

موارد زیر نباید در provider trace قرار گیرند:

- request body
- response body
- full ISO message
- raw provider payload
- secrets
- token
- PIN
- CVV2
- MAC key
- full PAN/card number

Provider log می‌تواند جزئیات امن و موردنیاز عملیات را پس از masking و sanitization ثبت کند. تنظیمات `sensitive-headers`، `sensitive-body-keys` و `max-body-log-length` باید رعایت شوند و نباید تضعیف شوند.

## ۱۱. قواعد Plugin

Wrapper اجرای plugin باید eventهای زیر را منتشر کند:

```text
PLUGIN_BEFORE_STARTED
PLUGIN_BEFORE_COMPLETED
PLUGIN_AFTER_STARTED
PLUGIN_AFTER_COMPLETED
PLUGIN_FAILED
```

Listener میزبان در `scm-web` آن‌ها را به span eventهای زیر تبدیل می‌کند:

```text
plugin.before.started
plugin.before.completed
plugin.after.started
plugin.after.completed
plugin.failed
```

Plugin می‌تواند برای log معمولی از `@Slf4j` استفاده کند. اتصال رسمی به observation، تولید structured observation log، افزودن span event و metric در ماژول میزبان انجام می‌شود.

Attributeهای plugin باید به metadata امن مانند نام، type، class، phase، order، outcome و duration محدود باشند. ورودی یا خروجی کامل provider نباید به span event افزوده شود.

## ۱۲. قانون Groovy plugin

Groovy plugin نباید raw `ScmObservation` دریافت کند و نباید مستقیماً span بسازد یا trace output بنویسد. تنها یک facade محدود در اختیار script قرار می‌گیرد؛ برای مثال:

```java
public interface PluginObservationContext {
    void traceEvent(String eventName, Map<String, ?> attributes);
    void logInput(String operation, Map<String, ?> importantParams);
    void logOutput(String operation, Map<String, ?> importantResult);
    void logFailure(String operation, Throwable throwable, Map<String, ?> safeContext);
}
```

Wrapper جاوا مسئول این موارد است:

- محدودکردن event name و attributeهای مجاز؛
- sanitization و masking قبل از log یا trace؛
- حذف payload، header و secretهای غیرمجاز؛
- تبدیل درخواست facade به event یا observation استاندارد میزبان.

Script فقط context امن و ضروری را ارسال می‌کند و نباید request/response کامل provider را وارد trace کند.

## ۱۳. اطلاعاتی که هرگز نباید log یا trace شوند

موارد زیر نباید به‌صورت خام در LOG، TRACE، AUDIT، METRIC یا SCM event ثبت شوند:

```text
raw JWT
Authorization header
Cookie
Set-Cookie
access token
refresh token
PIN
CVV2
MAC key
password
secret
full PAN/card number
raw provider payload
```

مقدار حساس باید حذف، mask یا با `[SECURE]` جایگزین شود. `SecretScrubbingObservationSanitizer` لایهٔ ایمنی نهایی است، اما وجود sanitizer مجوز ارسال دادهٔ حساس به API مشاهده‌پذیری نیست؛ داده باید از ابتدا حداقلی و امن باشد.

## ۱۴. مثال‌های کوتاه

### Normal SLF4J log

این log توسط تنظیمات `Logback` میزبان کنترل می‌شود و در صورت وجود، فیلدهای `MDC` را نیز همراه دارد:

```java
@Slf4j
public class RestProviderExecutor {
    public void call(String providerCode, String endpoint) {
        log.info("Calling provider code={} endpoint={}", providerCode, endpoint);
    }
}
```

### انتشار security event

Event فقط metadata امن request و error code را حمل می‌کند:

```java
scmEventPublisher.publish(
        ScmSecurityEvent.of(
                ScmSecurityEventType.TOKEN_INVALID,
                Map.of(
                        "http.method", request.getMethod(),
                        "url.path", request.getRequestURI(),
                        "client.ip", request.getRemoteAddr(),
                        "error.code", "invalid_token"
                )
        )
);
```

### انتشار provider event

Body درخواست و پاسخ در event قرار نمی‌گیرد:

```java
scmEventPublisher.publish(
        ScmProviderEvent.of(
                ScmProviderEventType.PROVIDER_RESPONSE_RECEIVED,
                Map.of(
                        "scm.provider.code", providerCode,
                        "scm.provider.endpoint", endpoint,
                        "scm.provider.duration_ms", durationMs,
                        "scm.provider.response_code", responseCode
                )
        )
);
```

### انتشار plugin event

Wrapper مدت اجرا و outcome را منتشر می‌کند:

```java
scmEventPublisher.publish(
        ScmPluginEvent.of(
                ScmPluginEventType.PLUGIN_BEFORE_COMPLETED,
                Map.of(
                        "plugin.name", pluginName,
                        "plugin.outcome", "success",
                        "plugin.duration_ms", durationMs
                )
        )
);
```

### مدیریت event در `scm-web`

Listener میزبان event خنثی را به observation log تبدیل می‌کند:

```java
@Component
@RequiredArgsConstructor
public class ScmWebSecurityObservationListener
        implements ApplicationListener<ScmSecurityEvent> {

    private final ScmObservation observation;

    @Override
    public void onApplicationEvent(ScmSecurityEvent event) {
        observation.log()
                .event()
                .category("security")
                .action(event.eventType())
                .outcome("failure")
                .attributes(event.attributes())
                .write();
    }
}
```

در پیاده‌سازی واقعی، outcome بر اساس event type تعیین می‌شود و listener می‌تواند trace event یا metric متناظر را نیز ثبت کند.

### ساخت `traceparent`

Caller فقط شناسهٔ trace و span جاری را ارسال می‌کند:

```java
String traceparent = "00-" + traceId + "-" + currentSpanId + "-01";
```

### ساخت spanهای اصلی

Scopeها به‌ترتیب بسته می‌شوند تا رابطهٔ والد/فرزند حفظ شود:

```java
try (ObservationScope gateway = observation.trace()
        .span("gateway.receive")
        .spanKind("server")
        .start()) {

    try (ObservationScope service = observation.trace()
            .span("service.execute")
            .spanKind("internal")
            .start()) {

        try (ObservationScope operation = observation.trace()
                .span("operation.call")
                .spanKind("client")
                .start()) {

            // provider/downstream call
        }
    }
}
```

در مسیر واقعی باید قبل از `close()`، outcome موفق یا failure و attributeهای نتیجه روی scope ثبت شوند.

## ۱۵. Registry، validation و خروجی‌ها

### Attribute registry

`ObservationAttributeRegistry` کاتالوگ attributeهای مجاز است و مسئول این موارد است:

- تشخیص attributeهای مجاز برای هر stream؛
- کنترل type و metadata؛
- حذف مقدار null، blank یا ثبت‌نشده؛
- اجرای سیاست sensitivity و masking؛
- جلوگیری از تعریف ناسازگار یک field در یک stream.

Attributeهای مشترک در starter تعریف می‌شوند و هر host یک `ObservationAttributeContributor` برای attributeهای اختصاصی خود ثبت می‌کند. Registration با Spring bean انجام می‌شود و Java SPI یا `META-INF/services` برای contributorها استفاده نمی‌شود.

کاتالوگ‌های مشترک:

```text
attributes.log.CommonLogAttributes
attributes.trace.CommonTraceAttributes
attributes.audit.ChangeEntityAuditAttributes
attributes.audit.ServiceExecuteAuditAttributes
attributes.metric.CommonMetricTags
```

Metric nameها جداگانه در `metrics.CommonMetricNames` قرار دارند. Attributeهای HTTP، provider، plugin، security، gateway و channel باید توسط host مربوط ثبت شوند.

### Document builder و validation

`ObservationDocumentFactory` فیلدهای پایهٔ LOG، TRACE و AUDIT را می‌سازد. `ObservationDocumentBuilder` مقدارها را ابتدا sanitize و سپس با registry آماده می‌کند. `ObservationRecordValidator` سند نهایی را بر اساس stream، record kind و presenceهای الزامی بررسی می‌کند.

`ObservationRecordKind` فقط context اعتبارسنجی است و در NDJSON نوشته نمی‌شود:

| kind | کاربرد |
| --- | --- |
| `PLAIN` | log معمولی |
| `CONTEXT` | context مربوط به runtime یا lifecycle |
| `EVENT` | رخداد ساخت‌یافته |
| `EXCEPTION` | خطا همراه throwable |
| `CHANGE` | تغییر entity یا state |

### Sanitizer

`SecretScrubbingObservationSanitizer` آخرین لایهٔ ایمنی است. این component JWT خام، Bearer token، Authorization value و assignmentهای واضح secret را در text، map، collection و array پاک‌سازی می‌کند. جایگزین استاندارد مقدار حساس `[SECURE]` است.

`ObservationAttributeSensitivity` سیاست registry را تعیین می‌کند:

- `RAW`: مقدار مجاز بدون masking؛
- `SECURE`: جایگزینی کامل با `[SECURE]`؛
- حالت‌های prefix/suffix: نمایش فقط بخش مجاز مقدار.

### Simple، JSONL و Logback

LOG simple and JSONL rendering use the same final observation document built from the Logback event, `MDC`, registered structured fields, marker, and throwable. An unregistered field is removed and an unknown field name produces a controlled warning. This keeps metadata, validation, exception handling, and secret sanitization identical across formats.

TRACE و AUDIT از مسیر `ObservationDocumentFactory` ساخته و با `ObservationRecordValidator` بررسی می‌شوند. Markerهای routing زیر فقط برای هدایت خروجی TRACE و AUDIT به appenderهای اختصاصی هستند:

```text
SCM_OBSERVATION_TRACE
SCM_OBSERVATION_AUDIT
```

For TRACE and AUDIT simple output, the existing JSON event payload is parsed and rendered as deterministic `key=value` fields. Malformed payloads fail safely inside Logback and still emit the trusted `stream=trace` or `stream=audit` identity. For each enabled destination, exact format selection activates only one appender.

### Metric و element risk

Metricها باید low-cardinality باشند. `correlation.id`، `trace.id`، `span.id`، username، شمارهٔ کارت یا حساب، شمارهٔ تلفن، token، OTP و sequence id نباید metric tag باشند.

مدل عمومی element risk و health در starter قرار دارد. Host نمونه‌های runtime را فراهم می‌کند و نتیجهٔ نهایی را به Micrometer می‌دهد:

```text
risk:   0=normal, 1=warning, 2=critical
health: 1=healthy, 0=unhealthy
```

محاسبهٔ threshold در application انجام می‌شود و Prometheus و Grafana فقط مقدار نهایی را مصرف می‌کنند. Metric همچنان از مسیر Actuator و Micrometer صادر می‌شود و در فایل LOG، TRACE یا AUDIT نوشته نمی‌شود.

## چک‌لیست پیاده‌سازی

- آیا host فقط policy و override واقعی را نگه داشته و technical defaultها را تکرار نکرده است؟
- آیا starter یا provider فقط event خنثی منتشر می‌کند؟
- آیا observation رسمی در host انجام می‌شود؟
- آیا همهٔ رکوردهای LOG، TRACE و AUDIT مقدارهای `event.stream`، `scm.metadata.namespace`، `scm.metadata.instance_id`، `scm.metadata.time_zone`، `scm.config.label`، `scm.observation.target.index`، `service.name` و `deployment.environment` را دارند؟
- آیا file routing از namespace استفاده می‌کند و از `scm.channel.code` برای نام فایل استفاده نمی‌شود؟
- آیا console output فقط در profile `dev` روشن است، بدون آن‌که AUDIT فقط به‌خاطر console فعال شود؟
- آیا formatها فقط lowercase و case-sensitive `simple|jsonl` هستند و simple line دقیقاً یک `stream=` دارد؟
- آیا فقط فایل‌های `.jsonl` به pipeline فعلی Filebeat داده می‌شوند و `.log` به‌عنوان خروجی human-readable باقی می‌ماند؟
- آیا `correlation.id` محلی است و به downstream ارسال نمی‌شود؟
- آیا فقط `traceparent` برای distributed trace ارسال می‌شود؟
- آیا ساختار `gateway.receive -> service.execute -> operation.call` حفظ شده است؟
- آیا legacy projection فقط در entry spanهای business و با `scm.observation.legacy.service.code` و `scm.observation.legacy.operation.code` صریح انجام می‌شود؟
- آیا payload و secret از event، trace، metric و log حذف یا mask شده‌اند؟
- آیا metric tagها محدود و low-cardinality هستند؟
