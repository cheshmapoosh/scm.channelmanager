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
- ساخت route ورودی بر اساس GatewayChannel و ProtocolHandler
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
- اجرای routing strategy مثل FIRST, FAIL_OVER, MULTI_OPERATION
- اتصال به operation/provider
```

Service layer نباید بداند request از REST آمده یا SOAP یا TCP. Service فقط با مدل داخلی SCM کار می‌کند.

---

## 5. اتصال Gateway به Service

در طراحی production-grade، برای هر service یک route explicit داریم.

مثال:

```text
scm.gateway.channel.mb.card-inquiry
    -> direct:scm.service.card-inquiry

direct:scm.service.card-inquiry
    -> service pipeline
    -> operation/provider
```

اتصال از gateway به service از طریق abstraction انجام می‌شود:

```text
ServiceRouteUriResolver
```

در اجرای فعلی، این resolver معمولاً direct endpoint تولید می‌کند:

```text
direct:scm.service.<service-code>
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
INBOUND_ROUTE
INBOUND_ROUTE_GROUP
API_DOCUMENTATION
SERVICE_DOMAIN_MEMBER
```

معنی هر کدام:

```text
INBOUND_ROUTE          -> تعریف یک route ورودی
INBOUND_ROUTE_GROUP    -> تعریف چند route ورودی برای یک service
API_DOCUMENTATION      -> مستند API مثل Swagger/OpenAPI/WSDL/spec
SERVICE_DOMAIN_MEMBER  -> عضویت یک ChannelServiceAccess در domain runtime
```

Mapping مفهومی از نسخه 8 به نسخه 9:

```text
REST           -> INBOUND_ROUTE
REST_MULTIPLE  -> INBOUND_ROUTE_GROUP
SWAGGER        -> API_DOCUMENTATION
```

رکوردهای قدیمی نسخه 8 نباید تغییر کنند. نسخه 9 باید با رکوردهای جدید مثل `channel.*` و `domain.*` کار کند.

---

## 7. ClientContract چیست؟

در SCM باید بین protocol و contract تفاوت بگذاریم.

```text
Protocol != Client Contract
```

مثلاً REST بودن الزاماً به معنی خروجی مدرن نیست. ممکن است یک client قدیمی با REST کار کند ولی فرمت legacy بخواهد.

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

مثال modern REST:

```json
{
  "method": "POST",
  "path": "/cards/inquiry",
  "contract": {
    "name": "modern-rest-v1",
    "requestDecoder": "jsonScmRequestDecoder",
    "responseEncoder": "jsonScmResponseEncoder",
    "faultEncoder": "restProblemDetailFaultEncoder"
  }
}
```

مثال legacy REST:

```json
{
  "method": "POST",
  "path": "/legacy/cards/inquiry",
  "contract": {
    "name": "legacy-mb-card-v1",
    "requestDecoder": "legacyMbCardRequestDecoder",
    "responseEncoder": "legacyMbCardResponseEncoder",
    "faultEncoder": "legacyMbCardFaultEncoder"
  }
}
```

Service layer نباید ClientContract را بشناسد. این تبدیل‌ها متعلق به gateway layer هستند.

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

اگر خواستی فرمت client قدیمی را پشتیبانی کنی:

```text
1. ClientContract را در Definition.details همان route تعریف کن.
2. decoder/encoder/faultEncoder مناسب legacy را معرفی کن.
3. service layer را تغییر نده.
```

اگر خواستی routing strategy یا provider call را تغییر بدهی:

```text
برو سراغ ServiceTargetRouter یا provider module.
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
