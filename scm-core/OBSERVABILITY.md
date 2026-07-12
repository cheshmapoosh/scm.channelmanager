# Observability - scm-core

## هدف

`scm-core` محل instrumentation منطق Service، Operation، Policy و Routing است.

## Trace

برای هر درخواست خارجی واقعی، `scm-core` فقط این سلسله‌مراتب business span را ایجاد می‌کند:

```text
gateway.receive
└── service.execute
    └── operation.call
```

`GatewayChannelLayerRouteBuilder`، `ServiceLayerRouteBuilder` و `OperationLayerRouteBuilder` مالک همین سه span هستند.
Plugin، security، provider، routing strategy و resilience policy child span جدید ایجاد نمی‌کنند؛ فعالیت‌های امن و لازم
آن‌ها به‌صورت event روی span همان لایه ثبت می‌شود.

## Camel context lifecycle

هر سه span با `startDetached()` شروع می‌شوند. `ObservationScope` و `TraceContext` واقعی sink فقط در propertyهای
محلی Camel Exchange نگهداری می‌شوند:

```text
scm.observation.scope.gateway
scm.observation.scope.service
scm.observation.scope.operation
```

عمر span به `ObservationScope.current()`، یک `TraceContextHolder` طولانی‌مدت یا Micrometer `SpanInScope`
وابسته نیست. بنابراین start و close می‌توانند روی threadهای متفاوت انجام شوند و close همچنان idempotent است.
برای کد synchronous داخل هر Camel processor، authentication و trace context فقط در مدت همان invocation bind
می‌شوند و در `finally` به مقدار قبلی برمی‌گردند؛ هیچ contextای از یک Exchange روی pooled thread باقی نمی‌ماند.

## Authentication and security trace data

`AuthenticationPluginHandler` شیء کامل Spring `Jwt` را در Camel header قرار نمی‌دهد. پس از enrichment مجاز
`gateway.receive`، فقط roleهای claim `aut` را در `ValidatedJwtBusinessContext` immutable نگه می‌دارد و
`loginData` حاوی `Jwt` را از authentication محلی Exchange حذف می‌کند. سایر fieldهای غیر-JWT authentication
برای سازگاری business حفظ می‌شوند. `jwt.aut` تنها کلید template سازگار است؛ دسترسی arbitrary به `jwt.*` و
claim map کامل پشتیبانی نمی‌شود.

Security eventهای Camel با Exchange و layer صریح ثبت می‌شوند و معمولاً به `gateway.receive` تعلق دارند.
Spring event listenerها LOG-only هستند و از ThreadLocal برای پیدا کردن span استفاده نمی‌کنند. Authorization
header، raw token و credential هیچ‌گاه به TRACE event منتقل نمی‌شوند.

Allowlist مربوط به trace JWT تغییر نکرده و فقط روی `gateway.receive` اعمال می‌شود؛ service، operation، plugin
و provider eventها آن را تکرار نمی‌کنند.

## Scheduled execution

در `scm-core` و `scm-web` entry point واقعی `@Scheduled`، timer/cron Camel یا Quartz پیدا نشد. بنابراین caller
تولیدی برای `scheduled.execute` اضافه نشده است. lifecycle reusable موجود فقط زمانی باید فراخوانی شود که اجرای
واقعی job شروع می‌شود، نه هنگام bean/route/trigger registration یا startup.

## Metric

Metricهای پیشنهادی:

```text
scm_service_executions_total
scm_operation_executions_total
scm_route_strategy_total
scm_resilience_rejections_total
```

Metric فقط Micrometer است، نه فایل.

## Audit

اگر API مدیریتی یا تغییر policy در core وجود داشت، Audit Event منتشر شود.
