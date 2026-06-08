<style>
@font-face {
  font-family: "Vazirmatn";
  src:
    local("Vazirmatn"),
    local("Vazirmatn Regular"),
    url("../assets/fonts/Vazirmatn-Regular.woff2") format("woff2"),
    url("./../assets/fonts/Vazirmatn-Regular.woff2") format("woff2");
  font-weight: 400;
  font-style: normal;
  font-display: swap;
}

@font-face {
  font-family: "Vazirmatn";
  src:
    local("Vazirmatn Medium"),
    url("../assets/fonts/Vazirmatn-Medium.woff2") format("woff2"),
    url("./../assets/fonts/Vazirmatn-Medium.woff2") format("woff2");
  font-weight: 500;
  font-style: normal;
  font-display: swap;
}

@font-face {
  font-family: "Vazirmatn";
  src:
    local("Vazirmatn Bold"),
    url("../assets/fonts/Vazirmatn-Bold.woff2") format("woff2"),
    url("./../assets/fonts/Vazirmatn-Bold.woff2") format("woff2");
  font-weight: 700;
  font-style: normal;
  font-display: swap;
}

.rtl-fa {
  direction: rtl;
  text-align: right;
  font-family: "Vazirmatn", Tahoma, sans-serif !important;
}

.rtl-fa h1,
.rtl-fa h2,
.rtl-fa h3,
.rtl-fa h4,
.rtl-fa h5,
.rtl-fa h6,
.rtl-fa p,
.rtl-fa li,
.rtl-fa blockquote,
.rtl-fa table,
.rtl-fa th,
.rtl-fa td,
.rtl-fa span,
.rtl-fa strong,
.rtl-fa em {
  font-family: "Vazirmatn", Tahoma, sans-serif !important;
}

.rtl-fa pre,
.rtl-fa code {
  direction: ltr;
  text-align: left;
  font-family: Consolas, "JetBrains Mono", monospace !important;
}
</style>

<div class="rtl-fa">

# راهنمای عملی راه‌اندازی سرویس در `scm-web`

> برای نمایش صحیح فونت در پیش‌نمایش Markdown در IntelliJ:
> `Settings -> Languages & Frameworks -> Markdown -> Custom CSS -> Load from`
> و فایل `scm-web/readme-preview.css` را انتخاب کن.

این سند برای کارشناس تازه‌وارد نوشته شده است تا بتواند از ابتدا تا انتها:

1. یک سرویس تعریف کند.
2. یک عملیات برای آن سرویس بسازد.
3. سرویس را روی یک `gateway channel` منتشر کند.
4. جریان اجرای عملیات را از روی سورس متوجه شود.

---

## 1) نقشه معماری

در `scm-web` سه لایه افقی داریم:

1. `gateway`
2. `service`
3. `operation`

ترتیب اجرای درخواست:

```text
Gateway Route -> Service -> Operation Route -> OperationTypeHandler -> Provider/Bean/Rest/Tcp
```

---

## 2) تحلیل کامل `OperationLayerRouteBuilder`

فایل مرجع:

`scm-core/src/main/java/ir/daneshrefah/scm/core/integration/operation/OperationLayerRouteBuilder.java`

### 2.1) شروع ساخت Route ها

در متد `configure`:

1. ابتدا `RuntimeRouteActivation` هدف‌های فعال runtime را می‌خواند.
2. برای هر gateway فعال، `RuntimeRoutePlanProvider` فقط service plan های همان runtime target را می‌سازد.
3. نام عملیات مورد نیاز از `servicePlans().service().serviceOperations` استخراج می‌شود.

```text
runtime targets -> gateway channel -> runtime route plan -> active service operations
```

نکته: `OperationLayerRouteBuilder` دیگر همه عملیات فعال دیتابیس را route نمی‌کند. اگر `scm-web` فقط برای `domain.card` بالا آمده باشد، فقط عملیات مورد نیاز service plan های `domain.card` ساخته می‌شوند.

4. برای هر عملیات مورد نیاز یک `direct route` ساخته می‌شود:

```text
from: direct:<operationName>
routeId: op.<operationName>
```

5. آبجکت عملیات داخل `exchange property` ست می‌شود:

```text
key: scmOperation
```

### 2.2) مدیریت خطا

برای هر route یک `onException` محلی تعریف می‌شود:

1. خطا `handled(true)` می‌شود.
2. خطا در `TraceUtils` ثبت می‌شود.
3. بدنه پیام با exception جایگزین می‌شود.
4. پیام به `GLOBAL_ERROR_HANDLER` می‌رود.

---

### 2.3) اجرای Plugin های قبل از هدف

قبل از اجرای هدف عملیات:

1. لیست Plugin های فاز `BEFORE` برای همان `operation` از `PluginResolverService` گرفته می‌شود.
2. برای هر plugin:
3. ابتدا `init` روی route اجرا می‌شود.
4. سپس `handle` در زمان هر درخواست اجرا می‌شود.

---

### 2.4) انتخاب هدف عملیات

متد `buildTarget` بر اساس `operation.type`، handler مناسب را از `operationTypeHandlers` پیدا می‌کند.

اگر handler پیدا نشود، خطا پرتاب می‌شود.

سپس `internalConfig` صدا زده می‌شود. این متد در `OperationTypeHandler` سه کار می‌کند:

1. `traceBeforeOperation`
2. اجرای `config` واقعی handler
3. `traceAfterOperation`

---

### 2.5) اجرای Plugin های بعد از هدف

بعد از اتصال target:

1. لیست Plugin های فاز `AFTER` خوانده می‌شود.
2. برای هر مورد، `init` و سپس `handle` ثبت می‌شود.

---

### 2.6) جمع‌بندی رفتاری `OperationLayerRouteBuilder`

`OperationLayerRouteBuilder` خودش منطق کسب‌وکاری عملیات را اجرا نمی‌کند. وظیفه آن:

1. ایجاد route فقط برای عملیات مورد نیاز runtime service plan های فعال.
2. تزریق خط‌مشی مشترک: خطا، trace، plugin.
3. سپردن مقصد واقعی عملیات به `OperationTypeHandler`.

---

## 3) `OperationTypeHandler` ها و معنای هر نوع عملیات

پوشه مرجع:

`scm-core/src/main/java/ir/daneshrefah/scm/core/integration/operation/handler`

انواع مهم:

1. `JAVA`
2. `REST`
3. `PROVIDER`
4. `BEAN`
5. `ATPS`

خلاصه:

1. `JAVA`: به `spring bean` وصل می‌شود و متد دارای `JavaService` را پیدا می‌کند.
2. `REST`: با `webclient` به آدرس بیرونی فراخوانی می‌زند.
3. `PROVIDER`: پیام را به فرمت `Map` استاندارد کرده و به `provider uri` می‌فرستد.
4. `BEAN`: ساده‌ترین حالت، `route.to("bean:<path>")`.
5. `ATPS`: برای مسیر `TCP` با `atps` و تنظیمات `TCP_CONFIG`.

---

## 4) مسیر مرحله‌به‌مرحله تعریف سرویس از صفر تا انتشار در `gateway channel`

این بخش مسیر اجرایی پیشنهادی برای کارشناس جدید است.

### گام 1) تعریف `operation provider`

اگر عملیات شما از نوع `PROVIDER` است، اول رکورد provider را بساز:

1. جدول: `REF.TBL_SCM_OPERATION_PROVIDER`
2. فیلدهای کلیدی:
3. `NAME`
4. `URI`
5. `ACTIVE = 1`

نمونه:

```text
NAME = hps-shetab7
URI  = shetab:hps-shetab7
```

---

### گام 2) تعریف `operation`

1. جدول: `REF.TBL_SCM_OPERATION`
2. نوع را مشخص کن:
3. `TYPE = PROVIDER` یا `JAVA` یا `REST` ...
4. به provider مناسب وصل کن: `OPERATION_PROVIDER_ID`
5. `ACTIVE = 1`

نکته مهم برای نوع `PROVIDER`:

`ProviderOperationTypeHandler` مقصد را از `operation.provider.uri` می‌گیرد. provider باید فعال باشد.

---

### گام 3) تعریف `operation definitions`

برای هر عملیات بر اساس نیاز، definition بساز و به عملیات وصل کن.

جدول اتصال:

`REF.TBL_SCM_OPERATION_DEFINITION`

نوع‌های رایج:

1. `REQUEST_TEMPLATE`
2. `RESPONSE_TEMPLATE`
3. `REST_CONFIG`
4. `TCP_CONFIG`

---

### گام 4) تعریف `plugin binding` برای عملیات

برای اجرای template و اعتبارسنجی‌ها:

1. scope را `OPERATION` بگذار.
2. روی operation موردنظر bind کن.
3. حداقل plugin های لازم را در definition plugin اضافه کن.

برای عملیات template-based معمولا این‌ها لازم است:

1. `operationTemplateTransformer` در فاز `BEFORE`
2. `operationTemplateTransformer` در فاز `AFTER`

---

### گام 5) تعریف `service`

در `REF.EB_SERVICE` سرویس را بساز:

1. `CODE`
2. `NAME`
3. `PUBLISH = 1`
4. `ROUTING_STRATEGY` (مثل `FIRST` یا `MULTI_OPERATION` یا `FAIL_OVER`)

---

### گام 6) اتصال سرویس به عملیات

در `REF.TBL_SCM_SERVICE_OPERATION` اتصال را ثبت کن:

1. `EB_SERVICE_ID`
2. `OPERATION_NAME`
3. `ACTIVE = 1`

نکته:

اگر `ROUTING_STRATEGY = FIRST` است، فقط یک عملیات فعال داشته باش.

---

### گام 7) اتصال سرویس به کانال

در `REF.CHANNEL_SERVICE_ACCESS` رکورد بساز:

1. `CHANNEL_ID`
2. `EB_SERVICE_ID`
3. `ACTIVE = 1`

این مرحله تعیین می‌کند سرویس روی کانال قابل استفاده باشد.

---

### گام 8) تعریف مسیر سرویس روی `gateway channel`

برای اینکه gateway بداند از چه URL یا پروتکل سرویس را باز کند:

1. `gateway channel` باید در `TBL_SCM_GATEWAY_CHANNEL` تعریف و فعال باشد.
2. برای هر service-channel یک definition در `TBL_SCM_CHN_SVC_DEFINITION` ثبت شود.
3. در حالت `REST`، مسیر، متد و تنظیمات امنیتی داخل definition می‌آید.

---

### گام 9) تعریف plugin در سطح کانال یا سرویس

برای کارهای امنیتی و عمومی:

1. scope `CHANNEL` برای سیاست‌های مشترک کانال.
2. scope `SERVICE` برای سیاست‌های خاص سرویس.

نمونه‌های رایج:

1. `jwtAuthPluginHandler`
2. `authorizationManagerPluginHandler`
3. `requestHeaderEnricherPlugin`

---

### گام 10) اجرای اپلیکیشن و آزمون

بعد از بالا آمدن `scm-web`:

1. درخواست تست بزن.
2. بررسی کن route ساخته شده باشد:

```text
direct:<operationName>
```

3. اگر خطا دیدی، از `GLOBAL_ERROR_HANDLER` و لاگ trace شروع کن.

---

## 5) مسیر واقعی پردازش یک درخواست

برای درک بهتر، ترتیب دقیق اجرای runtime:

1. `GatewayChannelLayerRouteBuilder` درخواست ورودی را می‌گیرد.
2. سرویس مقصد را بر اساس channel و service mapping پیدا می‌کند.
3. بر اساس `routingStrategy`، عملیات مناسب را انتخاب می‌کند.
4. به `direct:<operationName>` می‌فرستد.
5. `OperationLayerRouteBuilder` وارد عمل می‌شود.
6. plugin های `BEFORE` اجرا می‌شوند.
7. `OperationTypeHandler` مقصد واقعی را صدا می‌زند.
8. plugin های `AFTER` اجرا می‌شوند.
9. پاسخ به `GLOBAL_RESPONSE_HANDLER` برمی‌گردد.

---

## 6) چک‌لیست نهایی قبل از تحویل

1. operation فعال است.
2. service فعال و منتشرشده است.
3. `CHANNEL_SERVICE_ACCESS` فعال است.
4. service حداقل یک `service operation` فعال دارد.
5. برای `FIRST` دقیقا یک عملیات فعال تعریف شده.
6. برای نوع `PROVIDER`، provider فعال و `URI` معتبر دارد.
7. plugin های لازم در scope درست bind شده‌اند.
8. definition های مورد نیاز عملیات کامل هستند.
9. مسیر `gateway channel` با قرارداد کلاینت یکی است.
10. لاگ بدنه‌های حساس در سطح `INFO` فعال نیست.

---

## 7) خطاهای رایج و محل بررسی

1. خطا: عملیات route نشد.
2. بررسی:
3. `operation.active`
4. `service_operation.active`
5. `service.routingStrategy`

1. خطا: plugin پیدا نشد.
2. بررسی:
3. نام plugin در definition
4. map `pluginHandlers`

1. خطا: handler نوع عملیات پیدا نشد.
2. بررسی:
3. مقدار `operation.type`
4. وجود کلاس handler متناظر

1. خطا: عملیات provider اجرا نمی‌شود.
2. بررسی:
3. `operation.provider.active`
4. `operation.provider.uri`

---

## 8) نقطه شروع پیشنهادی برای کارشناس تازه‌وارد

ترتیب پیشنهادی کار:

1. یک عملیات ساده نوع `BEAN` یا `JAVA` بساز.
2. سرویس با `FIRST` بساز و به همان عملیات وصل کن.
3. سرویس را روی یک `gateway channel` در `REST` منتشر کن.
4. درخواست موفق بگیر.
5. سپس به سراغ عملیات `PROVIDER` برو و template/plugin را اضافه کن.

با این ترتیب، خطاها سریع‌تر جدا می‌شوند و مسیر یادگیری پایدارتر است.

</div>
