# مستندات Observation در SCM

## هدف

این فایل نقطهٔ ورود مستندات Observation پروژه است. مستندات به‌صورت ماژول‌محور نگهداری می‌شوند؛ هر ماژول مسئول توضیح دقیق استفادهٔ خودش از زیرساخت Observation است و قرارداد عمومی در `scm-observation-starter` قرار دارد.

معماری خروجی:

```text
LOG    -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
TRACE  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
AUDIT  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
METRIC -> Actuator -> Micrometer -> Prometheus -> Grafana
```

## مراجع اصلی

| موضوع | مرجع |
| --- | --- |
| قرارداد عمومی، variableها، registry، lifecycle و مسیر توسعه | `scm-observation-starter/README.md` |
| Servlet correlation و HTTP server observation | `scm-observation-servlet-starter/README.md` |
| استفاده در Config Server | `scm-config/OBSERVABILITY.md` |
| استفاده در Cache Server | `scm-cache/OBSERVABILITY.md` |
| استفاده در UAA | `scm-uaa/OBSERVABILITY.md` |
| استفاده در Web/Gateway و تعریف سرویس‌ها | `scm-web/OBSERVABILITY.md` |
| استفاده در CM Connector | `scm-cm-connector/OBSERVABILITY.md` |

## قواعد مستندسازی

1. قرارداد عمومی نباید به هیچ ماژول میزبان وابسته باشد.
2. رفتار اختصاصی هر میزبان فقط در مستند همان ماژول توضیح داده می‌شود.
3. تنظیمات runtime از طریق environment variable انجام می‌شود. propertyهای Spring قرارداد داخلی binding هستند و نباید به‌عنوان روش عملیاتی تنظیم معرفی شوند.
4. افزودن attribute جدید باید هم‌زمان در کد register و در مستند ماژول مالک ثبت شود.
5. تغییر در ساختار سند، نام variable، نام attribute، نوع Elasticsearch، span یا event بدون به‌روزرسانی مستند مربوطه کامل نیست.
6. اطلاعات محرمانه مانند token، password، Authorization، Cookie، OTP، PIN، CVV و secret در LOG، TRACE، AUDIT یا METRIC ثبت نمی‌شوند.

## تفکیک مسئولیت

```text
scm-observation-starter
  هستهٔ مستقل از transport، registry، validation، sanitizer، sink و lifecycle

transport adapter
  استخراج metadata پروتکل و اتصال آن به هسته؛ مانند Servlet یا Camel

host module
  انتخاب policy، variableها، attributeهای اختصاصی، eventها، spanها و integration عملیاتی
```

هر ماژول میزبان باید مشخص کند:

- چه signalهایی فعال است؛
- چه variableهایی استفاده می‌شود؛
- چه span و eventهایی تولید می‌شود؛
- چه attributeهایی مالکیت آن ماژول را دارد؛
- attribute جدید چگونه register می‌شود؛
- خطای پیکربندی در چه مرحله‌ای fail-fast می‌شود؛
- چه داده‌هایی مجاز یا ممنوع است.
