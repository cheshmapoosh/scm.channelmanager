# Observation در `scm-web`

## ۱. نقش ماژول

`scm-web` میزبان Gateway و pipelineهای Apache Camel است. Business TRACE فقط پس از resolve شدن request به یک route واقعی SCM ساخته می‌شود.

موارد زیر business span ایجاد نمی‌کنند:

```text
startup و bean initialization
route registration
health و readiness
Actuator
Swagger و documentation
static resources
admin/config endpointها
HTTP requestهای بدون route SCM
```

ماژول از این اجزا استفاده می‌کند:

```text
scm-observation-starter
scm-observation-servlet-starter
Camel gateway observation adapter در scm-core
```

Servlet adapter correlation/MDC را فراهم می‌کند؛ مالک business trace، Camel Gateway است.

## ۲. Signalهای مورد استفاده

| Signal | Policy |
| --- | --- |
| LOG | فعال |
| TRACE | فعال برای routeهای business |
| AUDIT | فقط برای eventهای قابل استناد و با فعال‌سازی صریح |
| METRIC | از مسیر Actuator/Micrometer |

## ۳. Variableهای مشترک

| Variable | نمونه | کاربرد |
| --- | --- | --- |
| `SCM_APP` | `scm-web` | نام application |
| `SCM_ENV` | `dev` | محیط اجرا |
| `SCM_LABEL` | `master` | Config label |
| `SCM_METADATA_NAMESPACE` | `local` | namespace |
| `SCM_METADATA_INSTANCE_ID` | `local-scm-web` | شناسه instance |
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
| `SCM_OBS_LOG_FILE_FORMAT` | `jsonl` |
| `SCM_OBS_TRACE_ENABLED` | `true` |
| `SCM_OBS_TRACE_CONSOLE_ENABLED` | `true` در dev |
| `SCM_OBS_TRACE_CONSOLE_FORMAT` | `jsonl` |
| `SCM_OBS_TRACE_FILE_ENABLED` | `true` |
| `SCM_OBS_TRACE_FILE_FORMAT` | `jsonl` |
| `SCM_OBS_AUDIT_ENABLED` | `false` یا `true` |
| `SCM_OBS_AUDIT_CONSOLE_ENABLED` | `true` در dev؛ به‌تنهایی Audit را فعال نمی‌کند |
| `SCM_OBS_METRIC_ENABLED` | `true` یا `false` |

## ۵. Variableهای HTTP و Gateway

### ۵.۱. Servlet adapter

Generic HTTP server span در `scm-web` نباید جایگزین business span شود:

```bash
export SCM_OBS_HTTP_SERVER_ENABLED=false
export SCM_OBS_HTTP_SERVER_MODE=channel-only
```

`HttpGatewayObservationFilter` فقط transport context و correlation را آماده می‌کند و `gateway.receive` نمی‌سازد.

### ۵.۲. هویت Gateway

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_GATEWAY_NAME` | `channel.nib` | نام Gateway runtime |
| `SCM_CHANNEL_CODE` | `IB` | channel پیش‌فرض |
| `SCM_CHANNELS` | `IB,MB` | channelهای runtime در صورت پشتیبانی |
| `SCM_RUNTIME_TARGET_KIND` | `CHANNEL` | نوع target |
| `SCM_GATEWAY_NAMES` | `channel.nib` | gatewayهای انتخاب‌شده |

### ۵.۳. Channel resolution و legacy policy

| Variable | نمونه | توضیح |
| --- | --- | --- |
| `SCM_WEB_OBS_GATEWAY_CHANNEL_PATH_PREFIX_MAPPINGS` | `/nib=NIB,/mb=MB` | نگاشت prefix مسیر به channel |
| `SCM_WEB_OBS_GATEWAY_CHANNEL_TRUSTED_HEADER_ENABLED` | `false` | اجازهٔ استفاده از header مورد اعتماد |
| `SCM_WEB_OBS_GATEWAY_CHANNEL_TRUSTED_HEADER_NAME` | `X-SCM-Channel` | نام header مورد اعتماد |
| `SCM_WEB_OBS_LEGACY_GATEWAY_ENABLED` | `false` | فعال‌سازی projection legacy برای routeهای نگاشت‌شده |
| `SCM_WEB_OBS_LEGACY_GATEWAY_ROUTE_MAPPINGS` | `route-1=SVC1:OP1` | نگاشت route به service/operation legacy |

### ۵.۴. Resource Server observation

| Variable | Default |
| --- | --- |
| `SCM_RESOURCE_SERVER_OBSERVATION_ENABLED` | `true` |
| `SCM_RESOURCE_SERVER_OBS_LOG_ENABLED` | `true` |
| `SCM_RESOURCE_SERVER_OBS_TRACE_ENABLED` | `true` |
| `SCM_RESOURCE_SERVER_OBS_METRIC_ENABLED` | `true` |

## ۶. نمونهٔ local

```bash
export SCM_APP=scm-web
export SCM_ENV=dev
export SCM_METADATA_NAMESPACE=local
export SCM_METADATA_INSTANCE_ID=local-scm-web
export SCM_GATEWAY_NAME=channel.nib
export SCM_CHANNEL_CODE=IB

export SCM_OBS_ENABLED=true
export SCM_OBS_LOG_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_AUDIT_ENABLED=false

export SCM_OBS_LOG_CONSOLE_ENABLED=true
export SCM_OBS_TRACE_CONSOLE_ENABLED=true
export SCM_OBS_LOG_CONSOLE_FORMAT=jsonl
export SCM_OBS_TRACE_CONSOLE_FORMAT=jsonl

export SCM_OBS_HTTP_SERVER_ENABLED=false
export SCM_OBS_HTTP_SERVER_MODE=channel-only
```

## ۷. سلسله‌مراتب span

یک request موفق معمولاً سه span دارد:

```text
gateway.receive                 server, root
└── service.execute             internal
    └── operation.call          client یا kind متناسب operation
```

مالک lifecycle:

| Span | شروع | پایان |
| --- | --- | --- |
| `gateway.receive` | بعد از resolve شدن request به route Gateway | بعد از ساخت response یا fault نهایی |
| `service.execute` | قبل از ورود به service layer | در مسیر success یا failure سرویس |
| `operation.call` | قبل از provider/downstream call | بعد از success یا failure operation |

هر سه span `trace.id` مشترک، `span.id` مستقل و `correlation.id` محلی یکسان دارند.

Scopeها در Camel Exchange نگهداری می‌شوند:

```text
scm.observation.scope.gateway
scm.observation.scope.service
scm.observation.scope.operation
```

Scopeها detached هستند؛ روی thread pool به‌صورت بلندمدت bind نمی‌شوند و دقیقاً یک بار بسته می‌شوند.

## ۸. Root Gateway Span

Attributeهای اصلی root span:

```text
@timestamp
trace.id
correlation.id
span.name
span.start_time
span.end_time
span.duration_ms
service.name
event.outcome

scm.gateway.channel.code

scm.client.id
scm.client.type
scm.client.channel.code
scm.client.channel.validated
scm.client.username
scm.client.address
scm.client.correlation.id

scm.auth.type
scm.auth.scheme
scm.auth.client.id
scm.auth.client.accept_address
scm.auth.subject.id
scm.auth.subject.username
scm.auth.issuer
scm.auth.audience
scm.auth.scopes
scm.auth.login_method
scm.auth.transaction_method

scm.service.code
scm.service.name
scm.service.version

scm.status.code
scm.status.outcome
scm.status.message
scm.status.description
```

`service.name` نام application اجراکننده است؛ برای مثال `scm-web`. `scm.service.*` مشخصات و داده‌های business service را نشان می‌دهد.

`scm.client.channel.validated` فعلاً فقط حاصل مقایسهٔ این دو مقدار است:

```text
scm.client.channel.code == scm.gateway.channel.code
```

در این مرحله audience احراز هویت در این boolean دخالت داده نمی‌شود.

## ۹. Eventهای پروتکل REST

### `gateway.request.received`

```text
http.method
url.path
url.query
http.request.body.size
http.request.header.user_agent
http.request.header.accept
http.request.header.accept_language
http.request.header.origin
http.request.header.referer
http.request.header.content_type
http.request.header.content_length
http.request.header.x_scm_client_correlation_id
event.outcome
```

`url.query` قبل از ثبت sanitize می‌شود. نام پارامترهای مرتبط با token، authorization، password، secret، api key، OTP، cookie، credential، PIN و CVV با `[REDACTED]` جایگزین می‌شود.

### `gateway.response.completed`

```text
http.status_code
http.response.body.size
http.response.header.content_type
event.outcome
```

`http.status_code` روی root span قرار نمی‌گیرد و فقط متعلق به response event است.

## ۱۰. Authentication enrichment

Authentication enrichment فقط از نتیجهٔ معتبر Spring Security استفاده می‌کند و header خام را decode نمی‌کند.

برای JWT:

```text
scm.auth.type   = jwt
scm.auth.scheme = bearer
```

نگاشت claimها:

| Claim | Attribute |
| --- | --- |
| `sub` | `scm.auth.subject.username` |
| `pid` | `scm.auth.subject.id` |
| `iss` | `scm.auth.issuer` |
| `aud` | `scm.auth.audience` |
| `scope` | `scm.auth.scopes` |
| `azp` یا `client_id` | `scm.auth.client.id` |
| `acp` | `scm.auth.client.accept_address` |
| `lam` | `scm.auth.login_method` |
| `tam` | `scm.auth.transaction_method` |

`pid` می‌تواند عدد یا رشته باشد و در trace به رشته تبدیل می‌شود.

برای mechanism جدید، یک implementation از `GatewayAuthenticationTraceContributor` اضافه می‌شود. Contributor نباید authentication یا business flow را fail کند؛ خطای آن فقط با نام contributor و failure type و بدون credential log می‌شود.

موارد ممنوع:

```text
raw JWT
Authorization
Cookie
password
client secret
full claim map
```

## ۱۱. تعریف اختیاری `OBSERVATION` برای هر سرویس Gateway

### ۱۱.۱. محل و هدف

برای استخراج داده‌هایی مانند source account، source card، destination، amount و status از Camel Exchange، یک definition با نوع زیر در `TBL_SCM_CHN_SVC_DEFINITION` ثبت می‌شود:

```text
OBSERVATION
```

این definition به همان Gateway/ChannelServiceAccess/Service مربوط است که route `INBOUND` آن سرویس را می‌سازد.

قواعد:

- وجود `OBSERVATION` اختیاری است؛
- نبود آن مانع ثبت route نمی‌شود؛
- برای هر service plan حداکثر یک definition مجاز است؛
- اگر وجود داشته باشد، هنگام registration همان Camel route validate و compile می‌شود؛
- definition نامعتبر باعث fail-fast شدن route registration می‌شود؛
- runtime extraction failure فقط rule را skip می‌کند و business request را fail نمی‌کند.

### ۱۱.۲. ساختار نسخه ۱

```json
{
  "version": 1,
  "trace": [
    {
      "attribute": "scm.service.source.card",
      "from": "request.body",
      "path": "/sourceCard",
      "required": true
    },
    {
      "attribute": "scm.service.destination.card",
      "from": "request.body",
      "path": "/destinationCard",
      "required": true
    },
    {
      "attribute": "scm.service.amount",
      "from": "request.body",
      "path": "/amount",
      "type": "long",
      "required": true
    },
    {
      "attribute": "scm.service.currency",
      "from": "constant",
      "value": "IRR"
    },
    {
      "attribute": "scm.status.code",
      "from": "response.body",
      "path": "/result/code"
    },
    {
      "attribute": "scm.status.message",
      "from": "response.body",
      "path": "/result/message"
    },
    {
      "attribute": "scm.service.document.number",
      "from": "response.body",
      "path": "/result/documentNumber"
    }
  ]
}
```

### ۱۱.۳. فیلدهای Rule

| Field | الزامی | توضیح |
| --- | --- | --- |
| `attribute` | بله | نام attribute ثبت‌شده در TRACE registry |
| `from` | بله | منبع استخراج |
| `path` | برای همهٔ sourceها به‌جز constant | مسیر یا نام field |
| `value` | فقط برای constant | مقدار ثابت |
| `type` | خیر | assertion نوع؛ منبع اصلی type، registry است |
| `required` | خیر؛ default=false | نبود مقدار در runtime warning تولید می‌کند |
| `default` | خیر | مقدار fallback با type ثبت‌شده |
| `overwrite` | خیر؛ default=false | اجازهٔ rule بعدی برای جایگزینی attribute تکراری |

### ۱۱.۴. Sourceهای مجاز

```text
request.body
request.header
request.query
request.path
response.body
response.header
exchange.property
constant
```

برای body، `path` در نسخهٔ فعلی JSON Pointer است:

```text
/sourceAccount
/transaction/amount
/result/status/code
```

برای header، query و path، مقدار `path` نام field است. برای `exchange.property` نام property بدون slash ابتدایی استفاده می‌شود.

### ۱۱.۵. Typeهای assertion

```text
string | text | keyword | date
long
int | integer
double | decimal | number
boolean | bool
```

`type` اختیاری است. اگر نوشته شود باید با type attribute ثبت‌شده سازگار باشد. تبدیل runtime همیشه بر اساس registry انجام می‌شود.

### ۱۱.۶. Attributeهای business ثبت‌شده

```text
scm.service.amount                  long
scm.service.currency                keyword
scm.service.source.card             keyword
scm.service.source.account          keyword
scm.service.destination.card        keyword
scm.service.destination.account     keyword
scm.service.document.number         keyword
scm.service.reference.number        keyword
scm.service.transaction.type        keyword

scm.status.code                     keyword
scm.status.outcome                  keyword
scm.status.message                  keyword
scm.status.description              keyword
```

در policy فعلی SCM، card، account و username به‌صورت کامل در attribute ثبت‌شده نگهداری می‌شوند و suffixهایی مانند `masked`، `fingerprint` یا `ref` استفاده نمی‌شود. دسترسی به trace، encryption، retention و محیط‌های غیرعملیاتی باید متناسب با این policy محدود شود.

### ۱۱.۷. Attributeهای غیرمجاز

Definition فقط می‌تواند scalarهای ثبت‌شده با prefixهای زیر را استفاده کند:

```text
scm.service.*
scm.status.*
```

موارد زیر قابل overwrite نیستند:

```text
trace.id
correlation.id
service.name
span.start_time
span.end_time
span.duration_ms
span.events
scm.service.code
scm.service.name
scm.service.version
scm.service.duration_ms
```

نام‌های مرتبط با token، authorization، password، secret، api key، cookie، credential، OTP، PIN و CVV رد می‌شوند.

### ۱۱.۸. Fail-fast هنگام route registration

موارد زیر route را بالا نمی‌آورند:

```text
بیش از یک OBSERVATION definition
JSON نامعتبر
version نامعتبر یا پشتیبانی‌نشده
trace غیرآرایه‌ای
rule غیر object
attribute ثبت‌نشده
source نامعتبر
path یا value الزامیِ مفقود
نوع نامعتبر یا ناسازگار
attribute سیستمی، unsafe یا object/collection
attribute تکراری بدون overwrite
constant/default ناسازگار با type
```

Log خطا با event زیر ثبت می‌شود:

```text
event=gateway.observation.definition.invalid
```

و شامل context پشتیبانی است:

```text
routeId
gatewayName
channelCode
serviceCode
serviceVersion
protocol
definitionId
ruleIndex
attribute
from
path
reason
```

### ۱۱.۹. Lifecycle استخراج از Camel Exchange

```text
route registration
  -> load optional OBSERVATION
  -> validate against ObservationAttributeRegistry
  -> enable stream caching when body extraction exists

request entry
  -> immutable request snapshot
  -> request event

onCompletion
  -> immutable final response snapshot
  -> execute all rules
  -> apply attributes to gateway.receive
  -> close gateway span
```

Snapshot request قبل از تغییر message گرفته می‌شود. `JsonNode` deep-copy، byte array clone و `StreamCache` بعد از copy reset می‌شود.

## ۱۲. Status semantics

```text
event.outcome      نتیجهٔ فنی اجرا
http.status_code    نتیجهٔ transport در response event
scm.status.code     کد business/banking
scm.status.outcome  نتیجهٔ business
```

نمونهٔ معتبر:

```text
event.outcome=success
http.status_code=200
scm.status.code=51
scm.status.outcome=failure
```

## ۱۳. مسیر توسعه

### افزودن attribute قابل استفاده در definition

1. semantic و type را مشخص کنید؛
2. attribute را در کلاس attributeهای مالک تعریف کنید؛
3. آن را به لیست contributor اضافه کنید تا در TRACE registry ثبت شود؛
4. از scalar بودن و مجاز بودن prefix مطمئن شوید؛
5. definition سرویس را با attribute جدید به‌روزرسانی کنید؛
6. route را reload/re-register کنید تا validation اجرا شود؛
7. mapping legacy و مستند را به‌روزرسانی کنید.

فقط افزودن نام جدید به JSON کافی نیست و با `unregisteredAttribute` fail-fast می‌شود.

### افزودن source جدید

1. semantic source را مشخص کنید؛
2. enum و extractor عمومی را توسعه دهید؛
3. snapshot و security boundary را بررسی کنید؛
4. مسیر validation startup را اضافه کنید؛
5. همهٔ سرویس‌ها باید بدون تغییر definition قبلی کار کنند.

### افزودن بخش‌های آینده به OBSERVATION

ساختار ریشه می‌تواند بعداً بخش‌های زیر را بپذیرد:

```text
log
audit
metric
```

پیاده‌سازی هر بخش باید handler مستقل، validation مستقل و قرارداد registry خودش را داشته باشد؛ بخش `trace` نباید به یک engine چندمنظورهٔ غیرقابل نگهداری تبدیل شود.

## ۱۴. فایل و ingestion

نمونهٔ فایل فعال local:

```text
trace-scm-scm-web-dev-local-local-scm-web.jsonl
```

فایل active تاریخ ندارد؛ تاریخ و roll index فقط به rolled file اضافه می‌شود. `scm.gateway.channel.code` یا `scm.client.channel.code` در نام فایل استفاده نمی‌شوند و فقط می‌توانند روی target index اثر بگذارند.
