# SCM Core Runtime Service Loading Guide

این مستند برای توسعه‌دهندگان نوشته شده است تا بدانند در SCM Core سرویس‌ها چگونه در زمان اجرا بالا می‌آیند و تفاوت اجرای سرویس‌ها در سطح `channel` و `service domain` چیست.

> این مستند مربوط به طراحی نسخه 9.x.x است. نسخه‌های 8.x.x همچنان با رکوردها و enumهای قدیمی کار می‌کنند.

---

## 1. ایده اصلی

در SCM، سرویس‌ها در زمان اجرا از روی یک `GatewayChannel.name` انتخاب می‌شوند.

در نسخه 9، convention نام‌گذاری به این شکل است:

```text
channel.<channel-code>
domain.<domain-code>
```

مثال:

```text
channel.mb
channel.ib
domain.card
domain.account
```

نکته مهم:

```text
name فقط برای lookup و تشخیص runtime target استفاده می‌شود.
protocol از GatewayChannel.protocolType خوانده می‌شود.
```

یعنی REST/SOAP/TCP بودن نباید از name یا از TYPE جدول definition برداشت شود.

---

## 2. تفاوت channel و domain

### 2.1 اجرای سرویس‌ها در سطح channel

وقتی `GatewayChannel.name` با `channel.` شروع شود، یعنی این runtime سرویس‌های یک کانال را بالا می‌آورد.

مثال:

```text
GatewayChannel.name = channel.mb
```

مسیر استخراج سرویس‌ها:

```text
GatewayChannel
    -> Channel
    -> CHANNEL_SERVICE_ACCESS
    -> Service
```

یعنی همه سرویس‌هایی که برای آن channel در `CHANNEL_SERVICE_ACCESS` فعال هستند، کاندید اجرای runtime می‌شوند.

### 2.2 اجرای سرویس‌ها در سطح service domain

وقتی `GatewayChannel.name` با `domain.` شروع شود، یعنی این runtime سرویس‌های یک دامنه سرویس را بالا می‌آورد.

مثال:

```text
GatewayChannel.name = domain.card
```

مسیر استخراج سرویس‌ها:

```text
GatewayChannel
    -> TBL_SCM_CHN_SVC_DEFINITION
    -> ChannelServiceAccess
    -> Service
```

در این حالت، سرویس‌ها از رکوردهای `TBL_SCM_CHN_SVC_DEFINITION` مربوط به همان `GatewayChannel` استخراج می‌شوند.

هدف این است که بتوانیم یک runtime مخصوص دامنه‌ای مثل کارت یا حساب داشته باشیم، بدون اینکه یک channel جعلی مثل `PUBLIC` یا `SVC` تعریف کنیم.

---

## 3. نقش RuntimeRoutePlanProvider

کدهای gateway و service نباید هر کدام جداگانه دیتابیس را بخوانند.

برای همین، یک لایه planning داریم:

```text
RuntimeRoutePlanProvider
```

این provider مسئول است که با توجه به `GatewayChannel.name` تشخیص دهد سرویس‌ها باید از کجا load شوند.

قانون:

```text
channel.*  -> load from CHANNEL_SERVICE_ACCESS
domain.*   -> load from TBL_SCM_CHN_SVC_DEFINITION
```

خروجی این provider باید یک plan قابل استفاده برای هر دو لایه باشد:

```text
RuntimeRoutePlan
    GatewayChannel
    RuntimeTargetKind
    List<RuntimeServicePlan>
```

و هر `RuntimeServicePlan` شامل اطلاعات service و access مربوطه است.

---

## 4. جداسازی Gateway Layer و Service Layer

### 4.1 Gateway Layer چه کاری می‌کند؟

Gateway layer فقط مسئول ورود و خروج درخواست است.

مسئولیت‌ها:

```text
- ساخت route ورودی بر اساس GatewayChannel و GatewayInboundRouteFactory
- خواندن protocol از GatewayChannel.protocolType
- تبدیل request client به مدل داخلی SCM
- تنظیم context اولیه مثل channelCode, serviceCode, gatewayName
- ارسال درخواست به service route با direct endpoint
- تبدیل پاسخ یا خطا به فرمت مناسب client
```

Gateway نباید این کارها را انجام دهد:

```text
- اجرای routing strategy سرویس
- انتخاب provider/operation
- اجرای pluginهای service-level
- تصمیم‌گیری business service
```

### 4.2 Service Layer چه کاری می‌کند؟

Service layer مسئول pipeline داخلی سرویس است.

مسئولیت‌ها:

```text
- ساخت route داخلی برای هر service
- RuntimeChannelGuard
- ChannelServiceAccessGuard
- اجرای pluginهای service-level
- audit plugin
- metrics plugin
- tracing/logging service-level
- اجرای routing strategy مثل FIRST، FAIL_OVER و CHAIN_ON_APPROVE
- اتصال به operation/provider
```

Service layer نباید بداند request از REST آمده یا SOAP یا TCP. Service فقط با مدل داخلی SCM کار می‌کند.

---

## 5. اتصال Gateway به Service

در طراحی production-grade، برای هر service یک route explicit داریم.

مثال:

```text
gw.dm.card.cardinquiry.v1
    -> direct:svc.dm.card.cardinquiry

direct:svc.dm.card.cardinquiry
    -> service pipeline
    -> direct:op.SVC_CARD_INQUIRY_TCP
    -> operation/provider
```

اتصال از gateway به service از طریق abstraction انجام می‌شود:

```text
ServiceRouteUriResolver
```

در اجرای فعلی، این resolver معمولاً direct endpoint تولید می‌کند:

```text
direct:<service-route-id>
```

مزیت این کار این است که اگر در آینده service layer به یک JVM یا microservice جدا منتقل شود، gateway فقط resolver جدید نیاز دارد.

---

## 6. نقش TBL_SCM_CHN_SVC_DEFINITION در نسخه 9

در نسخه 9، فیلد `TYPE` در جدول `TBL_SCM_CHN_SVC_DEFINITION` دیگر نباید معنی protocol بدهد.

Protocol از اینجا خوانده می‌شود:

```text
GatewayChannel.protocolType
```

معنی جدید `TYPE`، نقش یا purpose definition است.

مقادیر نهایی نسخه 9:

```text
INBOUND
API_DOC
SVC_DOMAIN_MEMBER
```

معنی هر کدام:

```text
INBOUND            -> one inbound gateway route for a service
API_DOC            -> API documentation/spec metadata
SVC_DOMAIN_MEMBER  -> ChannelServiceAccess membership in domain runtime
```

مقادیر قدیمی نسخه 8 فقط به عنوان داده تاریخی مطرح هستند و دیگر در enum جاوا برای runtime نسخه 9 وجود ندارند. Mapping مفهومی historical:

```text
REST           -> INBOUND
REST_MULTIPLE  -> historical multi-route container only; v9 uses multiple INBOUND rows
SWAGGER        -> API_DOC
```

Multiple gateway routes are modeled as multiple `INBOUND` definitions. The v9 runtime model has no group/container definition for inbound routes.

رکوردهای قدیمی نسخه 8 نباید تغییر کنند، اما runtime نسخه 9 باید با رکوردهای جدید مثل `channel.*` و `domain.*` و مقدارهای enum نهایی کار کند.

---

## 7. ClientContract چیست؟

در SCM باید بین protocol و contract تفاوت بگذاریم.

```text
Protocol != Client Contract
```

SCM نباید client را با برچسب‌هایی مثل legacy یا modern بشناسد. SCM فقط نسخه بیرونی Client Contract را می‌شناسد.

`ClientContract` مشخص می‌کند:

```text
- request چگونه به SCM Message تبدیل شود
- SCM Message چگونه به response client تبدیل شود
- SCMFault چگونه به error response client تبدیل شود
```

جای اصلی تعریف contract:

```text
TBL_SCM_CHN_SVC_DEFINITION -> Definition.details
```

قانون نسخه contract:

```text
/card/inquiry      -> v1
/v1/card/inquiry   -> v1
/v2/card/inquiry   -> v2
/v3/card/inquiry   -> v3
```

اگر `version` در `Definition.details` تعریف شده باشد و معتبر باشد، همان مقدار استفاده می‌شود. اگر `version` وجود نداشته باشد، gateway نسخه را از segment اول path با الگوی `/vN/` می‌خواند. اگر path نسخه نداشته باشد، نسخه پیش‌فرض `v1` است.

`v1` می‌تواند رفتار client-facing قدیمی CM را نگه دارد تا clientهای CM بدون حس کردن تغییر به SCM منتقل شوند:

```text
- همان URL
- همان request payload format
- همان response payload format
- همان error format
- همان HTTP status behavior، اگر برای protocol معنی داشته باشد
```

مثال ClientContractVersion v1:

```json
{
  "version": "v1",
  "method": "POST",
  "path": "/card/inquiry",
  "contract": {
    "name": "card-inquiry-v1",
    "requestDecoder": "cardInquiryV1RequestDecoder",
    "responseEncoder": "cardInquiryV1ResponseEncoder",
    "faultEncoder": "cardInquiryV1FaultEncoder"
  }
}
```

مثال ClientContractVersion v2:

```json
{
  "version": "v2",
  "method": "POST",
  "path": "/v2/card/inquiry",
  "contract": {
    "name": "card-inquiry-v2",
    "requestDecoder": "cardInquiryV2RequestDecoder",
    "responseEncoder": "cardInquiryV2ResponseEncoder",
    "faultEncoder": "cardInquiryV2FaultEncoder"
  }
}
```

`ContractStyle` عمداً بخشی از SCM نیست، چون SCM نباید legacy یا modern بودن client را بداند. `versionSelector` هم در مدل path-based فعلی لازم نیست، چون path و `Definition.details.version` نسخه contract را مشخص می‌کنند.

`routeId` gateway باید version را داشته باشد تا دو route برای یک service با نسخه‌های مختلف collision ندهند:

```text
card-inquiry-v1-route
card-inquiry-v2-route
```

Service layer نباید ClientContract را بشناسد. این تبدیل‌ها متعلق به gateway layer هستند.

Service route به صورت پیش‌فرض version-aware نیست:

```text
/card/inquiry     -> contract v1 -> direct:svc.dm.card.cardinquiry
/v2/card/inquiry  -> contract v2 -> direct:svc.dm.card.cardinquiry
```

Service layer فقط زمانی باید version-aware شود که رفتار business، operation یا provider واقعاً متفاوت باشد.

---

## 8. Guardها در Service Layer

### 8.1 RuntimeChannelGuard

این guard اختیاری است.

کار آن این است که بررسی کند این instance اجازه پذیرش درخواست از channel ورودی را دارد یا نه.

پیش‌فرض:

```text
همه channelها مجاز هستند.
```

Config نمونه:

```yaml
scm:
  runtime:
    channel-affinity:
      enabled: false
      allowed-channel-codes:
        - "*"
```

### 8.2 ChannelServiceAccessGuard

این guard بعد از RuntimeChannelGuard اجرا می‌شود.

کار آن این است که بررسی کند channel درخواست‌دهنده به service موردنظر دسترسی دارد یا نه.

مرجع این تصمیم:

```text
CHANNEL_SERVICE_ACCESS
```

---

## 9. Logging, Trace, Audit و Metrics

همه routeها باید log ساختاریافته داشته باشند.

فیلدهای مهم که باید در log و audit قابل جستجو باشند:

```text
traceId
spanId
correlationId
gatewayName
channelCode
serviceCode
serviceVersion
operationName
routeId
exchangeId
```

هدف پشتیبانی:

```text
همکاران پشتیبان بتوانند با traceId یا spanId در Elastic هم log و هم audit را پیدا کنند.
```

Audit plugin در Service layer اجرا می‌شود و audit را در فایل ذخیره می‌کند. فرمت پیشنهادی، اگر فرمت دیگری در پروژه تعریف نشده باشد:

```text
JSON Lines
```

Metrics مربوط به pluginهای service باید شامل count, duration و failure باشد.

---

## 10. قانون ساده برای توسعه‌دهنده جونیور

اگر خواستی سرویس‌ها را در سطح channel بالا بیاوری:

```text
1. GatewayChannel.name را با convention channel.<code> تعریف کن.
2. سرویس‌های channel از CHANNEL_SERVICE_ACCESS خوانده می‌شوند.
3. routeهای gateway با protocol همان GatewayChannel ساخته می‌شوند.
```

اگر خواستی سرویس‌ها را در سطح domain بالا بیاوری:

```text
1. GatewayChannel.name را با convention domain.<code> تعریف کن.
2. سرویس‌های domain از TBL_SCM_CHN_SVC_DEFINITION مربوط به همان GatewayChannel خوانده می‌شوند.
3. رکوردهای definition باید service/accessهای عضو domain را مشخص کنند.
```

اگر خواستی رفتار client-facing قدیمی CM را پشتیبانی کنی:

```text
1. ClientContractVersion v1 را در Definition.details همان INBOUND تعریف کن.
2. path بدون version مثل /card/inquiry را برای v1 نگه دار.
3. decoder/encoder/faultEncoder سازگار با CM قدیمی را معرفی کن.
4. service layer را تغییر نده.
```

اگر خواستی نسخه جدید client contract اضافه کنی:

```text
1. route جدید با path مثل /v2/card/inquiry بساز.
2. version همان route را v2 بگذار.
3. routeId باید version داشته باشد تا با v1 تداخل نکند.
4. service layer را تغییر نده.
```

اگر خواستی routing strategy یا provider call را تغییر بدهی:

```text
برو سراغ package مربوط به service routing یا provider module.
ServiceTargetRouter فقط dispatcher است و business rule نباید داخل آن قرار بگیرد.
Gateway را تغییر نده.
```

---

## 11. چیزهایی که نباید انجام شود

```text
- fake channel مثل PUBLIC یا SVC نساز.
- service domain را از ServiceCategory نگیر.
- protocol را از TYPE جدول definition تشخیص نده.
- legacy response format را وارد service layer نکن.
- business routing/provider logic را در gateway layer نگذار.
- operation layer را بی‌دلیل تغییر نده.
```

---

## CHAIN_ON_APPROVE Routing Strategy

### چه مشکلی را حل می‌کند؟

گاهی یک `Service` به چند `ServiceOperation` فعال وصل است و operationها باید به ترتیب اجرا شوند. در این حالت operation بعدی فقط وقتی اجرا می‌شود که نتیجه operation قبلی از نظر business قابل قبول یا **approved** باشد.

تفاوت strategyها:

```text
FIRST             -> دقیقا یک operation فعال را اجرا می‌کند.
FAIL_OVER         -> رفتار failover فعلی را برای operationهای فعال حفظ می‌کند.
CHAIN_ON_APPROVE  -> operationهای فعال را به ترتیب اجرا می‌کند و قبل از رفتن به مرحله بعد approval را می‌سنجد.
```

### در کد چگونه کار می‌کند؟

- `ServiceTargetRouter` فقط `RoutingStrategy` را به registry می‌دهد و هیچ `if/else` یا `switch` مربوط به strategy ندارد.
- `ServiceTargetRoutingRegistry` handler مناسب را پیدا می‌کند و handler تکراری، strategy خالی یا strategy پشتیبانی‌نشده را رد می‌کند.
- هر پیاده‌سازی `ServiceTargetRoutingHandler` مسئول یک strategy است.
- `ChainOnApproveRoutePlanFactory` هنگام ساخته‌شدن Camel route، operationهای فعال را به یک plan تغییرناپذیر تبدیل می‌کند.
- هر `RoutingStepPlan` از قبل operation، آدرس endpoint، request factory، و `ChainStepDecisionPolicy` همان مرحله را نگه می‌دارد.
- `ChainOnApproveStepConfigExtractor` تنظیمات هر مرحله را از JSON موجود در `ServiceOperation.definition.details` می‌خواند.
- برای هر service-operation فعال در `CHAIN_ON_APPROVE` وجود `Definition` و `Definition.details` الزامی است.
- فیلد `executionOrder` الزامی است، باید integer باشد، و ترتیب اجرای stepها را مشخص می‌کند.
- فیلد `decisionPolicy` اختیاری است. نبودن آن یعنی policy رسمی `DEFAULT_SUCCESS`.
- `decisionPolicy` نتیجه operation را به `CONTINUE`، `RETRY_LATER` یا `FAIL` تبدیل می‌کند. اگر وجود داشته باشد، باید string غیرخالی و code یک policy ثبت‌شده باشد.
- فقط `executionOrder` و `decisionPolicy` در JSON این step پشتیبانی می‌شوند. فیلد اضافه، JSON نامعتبر، `executionOrder` نامعتبر یا تکراری، و policy خالی یا ناشناخته باعث fail شدن ساخت route می‌شود.
- `ChainOnApproveServiceTargetRoutingHandler` plan آماده را به `ChainOnApproveRoutingEngine` می‌دهد. executor مشترک قبل از هر فراخوانی propertyهای `Message.SERVICE_OPERATION` و `Message.OPERATION_NAME` را تنظیم می‌کند.

## TASK_WORKFLOW routing strategy

`TASK_WORKFLOW` is documented in one canonical guide:

[Defining TASK_WORKFLOW services](docs/task-workflow-service.md)

Keep this section as a pointer only. The canonical guide owns the
TASK_WORKFLOW service-definition contract, inbound route boundary, runtime flow,
database relationships, supported step types, identifier normalization,
startup validation, examples, and troubleshooting.

## approved همیشه مساوی success نیست

`DEFAULT_SUCCESS` نتیجه استاندارد موفق SCM را `CONTINUE` می‌داند.

ممکن است یک خطای business مثل duplicate data برای ادامه chain قابل قبول باشد. برای این رفتار باید یک bean جدید از `ChainStepDecisionPolicy` با `code()` مشخص ساخته شود و همان code در JSON فیلد `decisionPolicy` مرحله قرار بگیرد:

```json
{
  "executionOrder": 10,
  "decisionPolicy": "DUPLICATE_DATA_APPROVED"
}
```

هیچ error code مربوط به duplicate داخل router hard-code نشده است.

### مثال

فرض کن سرویس سه operation فعال `A`، `B` و `C` دارد:

```text
A approved      -> B اجرا می‌شود
B approved      -> C اجرا می‌شود
C not approved  -> chain تمام می‌شود
```

اگر پاسخ `A` از نظر فنی error باشد ولی policy آن error را approved بداند، `B` همچنان اجرا می‌شود.

### تنظیم database

- `REF.EB_SERVICE.ROUTING_STRATEGY` strategy سرویس را مشخص می‌کند و برای این حالت باید دقیقا `CHAIN_ON_APPROVE` باشد.
- `REF.TBL_SCM_SERVICE_OPERATION` operationهای سرویس و اتصال هر مرحله به definition را نگه می‌دارد. فقط رکوردهای active وارد plan می‌شوند.
- `REF.TBL_SCM_SERVICE_OPERATION.DEFINITION_ID` به `REF.TBL_SCM_DEFINITION` اشاره می‌کند.
- `REF.TBL_SCM_DEFINITION.DETAILS` تنظیمات step را به شکل JSON نگه می‌دارد.
- هر service-operation فعال باید یک definition با `DETAILS` معتبر داشته باشد.
- فیلد `executionOrder` الزامی است و باید integer باشد.
- فیلد `decisionPolicy` اختیاری است و وقتی مقدار داشته باشد باید با `ChainStepDecisionPolicy.code()` یک Spring bean برابر باشد.
- نبودن `decisionPolicy` در JSON معتبر یعنی policy پیش‌فرض `DEFAULT_SUCCESS`. مقدار null، blank یا غیر-string نامعتبر است.
- `DEFAULT_SUCCESS` نتیجه موفق استاندارد SCM را ادامه می‌دهد، نتیجه موقت/نامشخص را `RETRY_LATER` و failure قطعی را `FAIL` می‌کند.
- نبودن definition، null/blank بودن `DETAILS`، JSON نامعتبر، فیلد پشتیبانی‌نشده، `executionOrder` نامعتبر یا تکراری، یا `decisionPolicy` نامعتبر/ناشناخته هنگام startup/ساخت route باعث fail شدن application می‌شود.

فرمت `DETAILS` برای هر step:

```json
{
  "executionOrder": 10,
  "decisionPolicy": "DUPLICATE_DATA_APPROVED"
}
```

مسیر resolve شدن policy:

```text
TBL_SCM_SERVICE_OPERATION.DEFINITION_ID
  -> TBL_SCM_DEFINITION.DETAILS
  -> executionOrder
  -> decisionPolicy
  -> ChainStepDecisionPolicyRegistry
  -> ChainStepDecisionPolicy bean
  -> RoutingStepPlan
```

فیلد `ServiceEntity.routingStrategy` با `EnumType.STRING` ذخیره می‌شود. مقدار database:

```text
CHAIN_ON_APPROVE
```

مثال:

```sql
UPDATE REF.EB_SERVICE
SET ROUTING_STRATEGY = 'CHAIN_ON_APPROVE'
WHERE CODE = 'YOUR_SERVICE_CODE';
```

طول این مقدار از محدودیت فعلی ستون (`20`) کمتر است. چون enum به شکل string ذخیره می‌شود و seed/migration محدودکننده‌ای برای لیست strategyها در repository وجود ندارد، برای اضافه شدن این مقدار migration جدا لازم نیست.

مثال تنظیم دو مرحله:

```text
EB_SERVICE.ROUTING_STRATEGY = CHAIN_ON_APPROVE

Step 1:
  operation = create-customer
  definition.details = {"executionOrder":10,"decisionPolicy":"DUPLICATE_DATA_APPROVED"}

Step 2:
  operation = create-account
  definition.details = {"executionOrder":20}
```

معنی مثال:

- `create-customer` اول اجرا می‌شود و از custom approval با کد `DUPLICATE_DATA_APPROVED` استفاده می‌کند.
- `create-account` دوم اجرا می‌شود و از رفتار پیش‌فرض `DEFAULT_SUCCESS` استفاده می‌کند.
- policy هر service-operation مستقل است؛ دو مرحله یک سرویس می‌توانند policy متفاوت داشته باشند.
- اگر bean مربوط به `DUPLICATE_DATA_APPROVED` وجود نداشته باشد، application هنگام ساخت route fail می‌شود، نه هنگام اولین request.

---

## CMNEW-119 Runtime Clarifications

- New deployments should use `scm.runtime.gateway-name` as the runtime key. `scm.app-name` is still read only as a legacy fallback.
- For `domain.*` runtimes, only `SVC_DOMAIN_MEMBER` rows define membership. `INBOUND` and `API_DOC` never add a service to a domain.
- A domain runtime creates one service route per `Service`. If several `SVC_DOMAIN_MEMBER` rows point at the same service for different channels, the runtime keeps those member `ChannelServiceAccess` records as metadata and still builds only one service route.
- Every active `channel.*` and `domain.*` service plan must have at least one `INBOUND` definition and at least one `API_DOC` definition. `SVC_DOMAIN_MEMBER` is membership only, and `API_DOC` does not expose a gateway route.
- Multiple gateway routes for the same service are modeled as multiple `INBOUND` definitions.
- Client contracts belong on `INBOUND`. A `contract` under `SVC_DOMAIN_MEMBER` or `API_DOC` is ignored and logged as a warning.
- Client contract version is path-based. A route without `/vN/` is `v1`; a route that starts with `/v2/` is `v2`; explicit `Definition.details.version` wins when valid.
- `ContractStyle` is intentionally not part of SCM, and `versionSelector` is not required in the current path-based phase.
- Gateway route IDs include the contract version, while service route URIs stay version-agnostic by default.
- The current client-contract response path is REST-only. SOAP/TCP need protocol-specific request and response encoders before they can share the global response contract route.
- `RuntimeChannelGuard` and `ChannelServiceAccessGuard` use the incoming channel code from the exchange/header. Channel codes are trimmed and lower-cased with `Locale.ROOT` before runtime guard comparisons. `ChannelServiceAccessGuard` resolves the current `ChannelServiceAccess` from the service access repository and stores it in `Message.CHANNEL_SERVICE_ACCESS`.
- Audit plugin entries show plugin execution points. The service route also writes a final `phase=SERVICE` audit event for the service success or failure outcome.
