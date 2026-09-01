# طراحی فعال‌سازی مستندات Markdown و Swagger در UAA و Web

## وضعیت و دامنه

| مورد | مقدار |
|---|---|
| وضعیت | طراحی نهایی؛ هنوز پیاده‌سازی نشده است |
| تاریخ | ۱۴۰۵/۰۶/۰۴ - 2026-08-26 |
| مصرف‌کنندگان | `scm-uaa` و `scm-web` |
| خروجی | Catalog، فایل خام Markdown، OpenAPI JSON و Swagger UI کنترل‌شده |
| اصل امنیتی | Production به‌صورت پیش‌فرض خاموش؛ در صورت فعال‌سازی فقط روی ingress داخلی و با `docs.read` |

---

## ۱. تصمیم معماری

دو نوع مستند از یکدیگر جدا می‌شوند:

| نوع | نقش | Source of truth |
|---|---|---|
| Release documentation | قرارداد نسخه‌دار Markdown/OpenAPI همراه همان artifact | فایل‌های immutable در classpath ماژول |
| Runtime SpringDoc | مشاهده endpointهای همان Runtime و کنترل drift | مدل تولیدشده در زمان اجرا |

`scm-docs-starter` Catalog و محتوای فایل‌های Markdown/OpenAPI را منتشر می‌کند. Swagger UI فقط viewer کنترل‌شده OpenAPI است. UI یا SpringDoc Runtime منبع رسمی قرارداد Production نیستند.

نتیجه:

- Contract رسمی هر نسخه داخل JAR همان ماژول قرار می‌گیرد.
- DB در `scm-web` فقط metadata و reference به artifact را نگه می‌دارد، نه نسخه پراکنده و قابل‌تغییر قرارداد.
- Swagger UI فقط specهای ثبت‌شده با `docId` را باز می‌کند و URL دلخواه از Query String نمی‌پذیرد.
- فعال بودن Documentation هیچ وابستگی به availability مسیرهای تراکنشی ایجاد نمی‌کند.

---

## ۲. وضعیت فعلی

### `scm-docs-starter`

قابلیت‌های موجود:

```text
GET /docs                  HTML catalog
GET /docs/api              grouped runtime API catalog
GET /docs/api/{docId}      raw document content
```

نوع‌های `MARKDOWN` و `OPENAPI_JSON` از قبل پشتیبانی می‌شوند. Starter فایل Markdown را به HTML تبدیل نمی‌کند؛ محتوای raw با media type مناسب برمی‌گردد.

ریسک فعلی: `scm.docs.enabled` پیش‌فرض `true` و AutoConfiguration دارای `matchIfMissing=true` است. بنابراین اضافه‌کردن dependency بدون Security/Config صریح ممکن است endpoint ناخواسته بسازد.

### `scm-web`

- dependency به `scm-docs-starter` از قبل وجود دارد؛
- provider مربوط به `API_DOC`های Runtime و cache catalog وجود دارد؛
- فایل/Config صریح برای مجموعه مستندات جدید وجود ندارد؛
- Security chain فعلی `/docs` را صریح match و protect نمی‌کند؛
- Swagger UI قدیمی می‌تواند URL را از Query String بگیرد که برای Production قابل قبول نیست.

### `scm-uaa`

- SpringDoc از قبل وجود دارد؛
- dependency به `scm-docs-starter` وجود ندارد؛
- مسیرهای فعلی SpringDoc زیر `/public` قرار گرفته‌اند؛
- فایل‌های نسخه‌دار Markdown/OpenAPI برای قرارداد RFC 7523 بسته‌بندی نشده‌اند؛
- Security chainهای فعلی `/docs` را صریح پوشش نمی‌دهند.

---

## ۳. قرارداد URL هدف

در هر application و روی host خودش:

| مسیر | خروجی |
|---|---|
| `GET /docs` | Catalog HTML مستندات ثبت‌شده |
| `GET /docs/api` | Catalog JSON گروه‌بندی‌شده |
| `GET /docs/api/{docId}` | محتوای raw Markdown یا OpenAPI JSON |
| `GET /docs/ui/{docId}` | Swagger UI اختیاری برای `OPENAPI_JSON` ثبت‌شده |
| `GET /v3/api-docs` | SpringDoc Runtime، فقط اگر feature جدا فعال باشد |
| `GET /swagger-ui/index.html` | UI مربوط به Runtime SpringDoc، فقط در محیط مجاز |

سه مسیر اول قرارداد فعلی Starter هستند. `/docs/ui/{docId}` قابلیت هدف و هنوز نیازمند پیاده‌سازی است.

تفکیک Host باعث می‌شود یک URL یکسان معنای محلی داشته باشد:

```text
https://<uaa-internal-host>/docs
https://<web-internal-host>/docs
```

در صورت داشتن context path یا reverse proxy prefix، تمام `href`ها باید از request context/forwarded prefix امن ساخته شوند و `/docs` به‌صورت hard-coded در لینک‌ها تکرار نشود.

---

## ۴. بسته‌بندی مستندات UAA

ساختار هدف:

```text
scm-uaa/src/main/resources/scm-docs/
  oauth2/v1/
    integration-guide.md
    openapi.json
  fararefah/v1/
    rfc7523-guide.md
    openapi.json
```

شناسه‌ها:

```text
scm-uaa.oauth2.v1.MARKDOWN.integration-guide
scm-uaa.oauth2.v1.OPENAPI_JSON.openapi-json
scm-uaa.fararefah.v1.MARKDOWN.rfc7523-guide
scm-uaa.fararefah.v1.OPENAPI_JSON.openapi-json
```

در UAA لازم است dependency زیر در فاز پیاده‌سازی اضافه شود:

```gradle
implementation project(':scm-docs-starter')
```

وجود dependency کافی است و annotation اختصاصی لازم نیست؛ اما فعال‌سازی فقط با config صریح و Security chain انجام می‌شود.

OpenAPI فرارفاه باید Token Endpoint و پارامترهای زیر را توصیف کند، بدون JWT یا Key واقعی:

```text
client_id
client_assertion_type
client_assertion
grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer
assertion
scope
```

SpringDoc فعلی UAA ابزار مشاهده Runtime است. قرارداد versioned فوق با تغییر Runtime overwrite نمی‌شود.

---

## ۵. بسته‌بندی مستندات Web و رسید بانکی

دو منبع در Web وجود دارد:

### مستندات static ماژول

```text
scm-web/src/main/resources/scm-docs/
  platform/v1/
    integration-guide.md
    openapi.json
```

### مستندات Runtime سرویس‌ها

Provider فعلی Web مسیرهای Runtime را فقط زیر `services/` می‌پذیرد. ساختار رسید:

```text
scm-web/src/main/resources/services/banking/bank-receipt/v1/
  api-docs.json
  markdown/integration-guide.md
  openapi/openapi.json
```

`ChannelServiceDefinitionEntity` با نوع `API_DOC` به Definition مربوطه متصل می‌شود. DB فقط این metadata/reference را نگه می‌دارد:

```json
{
  "detailsRef": {
    "type": "CLASSPATH",
    "path": "services/banking/bank-receipt/v1/api-docs.json"
  }
}
```

فایل `api-docs.json` descriptor فایل Markdown و OpenAPI را در همان نسخه معرفی می‌کند. نمونه شناسه‌ها:

```text
scm-web.bank-receipt.v1.MARKDOWN.integration-guide
scm-web.bank-receipt.v1.OPENAPI_JSON.openapi-json
```

قرارداد API با طراحی [`../scm-web/BANK_RECEIPT_SERVICE_DESIGN.md`](../scm-web/BANK_RECEIPT_SERVICE_DESIGN.md) همگام است.

---

## ۶. تنظیمات صریح

هر دو برنامه باید block مستقل داشته باشند؛ اتکا به default Starter ممنوع است:

```yaml
scm:
  docs:
    enabled: ${SCM_DOCS_ENABLED:false}
    base-path: /docs
    module-code: scm-uaa # در Web برابر scm-web
    classpath-root: scm-docs
    api:
      enabled: ${SCM_DOCS_API_ENABLED:true}
      fail-fast: ${SCM_DOCS_API_FAIL_FAST:false}
```

در یک نسخه سازگار، default داخلی Starter نیز از `true` به `false` تغییر می‌کند؛ پیش از آن، مصرف‌کنندگان موجود باید explicit config بگیرند.

`scm.docs.api.enabled` در پیاده‌سازی فعلی فقط API catalog Runtime را کنترل می‌کند و مستند static را خاموش نمی‌کند. اگر نیاز به کنترل مستقل باشد، feature flagهای جدا تعریف می‌شوند:

```text
SCM_DOCS_ENABLED
SCM_DOCS_RUNTIME_OPENAPI_ENABLED
SCM_DOCS_SWAGGER_UI_ENABLED
```

### ماتریس محیط

| محیط | Docs | Runtime OpenAPI/UI | دسترسی |
|---|---:|---:|---|
| local/dev | روشن | روشن | anonymous فقط روی loopback/internal یا Token معتبر |
| test/pilot | روشن | حسب نیاز | Token معتبر با `docs.read` |
| production | خاموش پیش‌فرض | خاموش پیش‌فرض | فقط فعال‌سازی صریح + ingress داخلی + `docs.read` |

در Production دو شرط مستقل لازم است: مسیر از ingress عمومی قابل دسترسی نباشد و برنامه نیز authorization را enforce کند.

---

## ۷. Security Design

برای هر برنامه یک `SecurityFilterChain` اختصاصی با order مشخص مسیرهای Documentation را match می‌کند:

```text
/docs/**
/docs/ui/**
/docs/assets/**
/v3/api-docs/**
/swagger-ui/**
/public/v3/api-docs/**     # فقط دوره مهاجرت UAA
/public/index.html         # فقط دوره مهاجرت UAA
```

رفتار strict:

- فقط `GET` و `HEAD`؛
- درخواست بدون Token برابر `401` و بدون redirect به Login؛
- Token بدون `SCOPE_docs.read` برابر `403`؛
- Feature خاموش برابر `404`؛
- CSRF برای endpointهای read-only موضوعیت عملی ندارد، ولی methodهای دیگر رد می‌شوند؛
- session ایجاد نمی‌شود و chain stateless است؛
- issuer و audience خود application اعتبارسنجی می‌شوند.

Scope پیشنهادی:

```text
docs.read
```

Client فرارفاه برای سرویس رسید این Scope را به‌صورت پیش‌فرض ندارد. مستندات داخلی با Client/Token مدیریتی جدا مشاهده می‌شوند.

### Headerهای پاسخ

برای Markdown/OpenAPI محافظت‌شده:

```text
X-Content-Type-Options: nosniff
Cache-Control: private, no-cache
Content-Disposition: inline; filename="<safe-file-name>"
```

برای HTML/Swagger UI:

```text
Content-Security-Policy: default-src 'self'; frame-ancestors 'none'; object-src 'none'
Referrer-Policy: no-referrer
X-Content-Type-Options: nosniff
```

CDN، font، JavaScript و CSS خارجی استفاده نمی‌شوند.

---

## ۸. طراحی امن Swagger UI

UI فقط `docId` معتبر از Registry می‌پذیرد:

```text
/docs/ui/scm-web.bank-receipt.v1.OPENAPI_JSON.openapi-json
```

Flow:

```text
docId
  -> validate safe identifier
  -> lookup descriptor in ScmDocsRegistry
  -> require type OPENAPI_JSON
  -> resolve same-origin /docs/api/{docId}
  -> render local Swagger UI
```

ممنوع:

- `?url=https://...` یا هر URL دلخواه؛
- remote validator؛
- remote `$ref`؛
- `servers` با host دلخواه محیط؛
- ذخیره Authorization در local storage؛
- نمایش Token/Secret نمونه واقعی.

تنظیمات هدف:

```text
validatorUrl = null
queryConfigEnabled = false
persistAuthorization = false
tryItOutEnabled = false    # test/pilot/prod
```

در local اگر Try it out لازم باشد، فقط با profile جدا فعال می‌شود. برای UI انسانی UAA از Authorization Code + PKCE استفاده می‌شود؛ `private_key_jwt`، Private Key یا Client Secret فرارفاه داخل مرورگر قرار نمی‌گیرد.

Security scheme در OpenAPI باید HTTP Bearer/OAuth2 درست باشد. تعریف عمومی `apiKey` روی Header `Authorization` و requirement سراسری برای همه operationها اصلاح می‌شود؛ endpointهای public و protected به‌صورت operation-level مشخص می‌شوند.

---

## ۹. Catalog یکپارچه

HTML `/docs` در مدل هدف هر دو گروه را نشان می‌دهد:

```text
Static module documents
Runtime service/API documents
```

قرارداد JSON موجود `/docs/api` برای سازگاری حفظ می‌شود. اگر Catalog JSON یکپارچه لازم باشد، endpoint versioned جدید تعریف می‌شود و معنای endpoint موجود بدون Versioning عوض نمی‌شود.

قواعد Registry:

- `docId` در کل application یکتا است؛
- duplicate ID در strict mode خطای startup/health می‌دهد، نه انتخاب بی‌صدای اولین مورد؛
- path traversal، slash، backslash، `..` و null byte رد می‌شوند؛
- media type با نوع descriptor تطبیق داده می‌شود؛
- حداکثر اندازه فایل و زمان load محدود است؛
- فقط classpath و providerهای allowlisted استفاده می‌شوند؛ remote URL provider پیش‌فرض وجود ندارد.

نبود یا خرابی Document هرگز route بانکی یا readiness اصلی تراکنش را از کار نمی‌اندازد؛ health مجزای Docs degradation را گزارش می‌کند. فقط در CI و profile authoring، `fail-fast=true` استفاده می‌شود.

---

## ۱۰. Versioning و Release Governance

- پوشه، `docId`، `info.version` در OpenAPI، نسخه endpoint و metadata DB باید یکسان باشند.
- فایل نسخه منتشرشده overwrite نمی‌شود؛ نسخه جدید کنار نسخه قبل قرار می‌گیرد.
- حذف نسخه قدیمی فقط پس از پایان support window و ثبت release note انجام می‌شود.
- `servers` در OpenAPI relative یا allowlisted است و hostname محیط hard-code نمی‌شود.
- فایل‌ها UTF-8 و line ending ثابت دارند.
- Contract رسمی همراه Boot JAR منتشر و checksum آن در build ثبت می‌شود.
- CI محتوای Boot JAR، لینک descriptor، JSON schema/OpenAPI validity و drift را کنترل می‌کند.

Drift policy:

```text
versioned OpenAPI vs approved contract       must match
runtime SpringDoc vs versioned OpenAPI       report differences
breaking change without API version change  fail build
```

---

## ۱۱. Observability و Audit

مستندات read-only هستند و نباید raw Token یا محتوای سند را log کنند.

Log/Trace امن:

```text
module_code
doc_id
document_type
outcome
authenticated_client
authenticated_subject_digest
trace_id
```

ممنوع:

- Authorization Header یا JWT؛
- assertion و key material؛
- query string حاوی URL؛
- بدنه OpenAPI/Markdown در Log؛
- نمونه‌های بانکی واقعی.

Audit دسترسی به Documentation در Production فقط در صورت الزام Governance ثبت می‌شود و از Application Log جدا است. Metric فقط با Micrometer و tagهای کم‌تنوع مانند module/type/outcome منتشر می‌شود.

---

## ۱۲. تست‌های پذیرش

### Starter

- `enabled=false` هیچ bean/endpoint مستندی ایجاد نکند؛
- Markdown و OpenAPI Content-Type و filename امن داشته باشند؛
- duplicate ID، traversal، null byte و media type ناسازگار رد شوند؛
- Catalog static و Runtime behavior مشخص داشته باشند؛
- context path و forwarded prefix در href حفظ شوند.

### UAA

- dependency و AutoConfiguration فقط با flag صریح endpoint بسازند؛
- guide و OpenAPI RFC 7523 از classpath resolve شوند؛
- strict profile برای anonymous برابر `401` و Scope اشتباه برابر `403` باشد؛
- Runtime SpringDoc و Swagger UI تابع flag مستقل باشند؛
- مسیرهای legacy `/public` پس از migration حذف یا همان Policy را enforce کنند.

### Web

- `API_DOC` رسید بانکی به هر دو فایل MD/OpenAPI resolve شود؛
- path فقط زیر `services/` پذیرفته شود؛
- خرابی/نبود Docs route تراکنشی رسید را مختل نکند؛
- Cache invalidation هنگام تغییر metadata درست باشد؛
- Swagger UI نتواند URL خارجی بارگیری کند.

### Contract و امنیت

- OpenAPI با validator مستقل معتبر باشد؛
- endpoint RFC 7523 و سرویس رسید با Scope/Audience صحیح توصیف شوند؛
- Boot JAR واقعاً شامل فایل‌های versioned باشد؛
- `servers` و `$ref` خارجی ممنوع باشند؛
- هیچ Secret، JWT واقعی، PAN، PIN، OTP، Account یا داده مشتری در فایل‌ها و exampleها نباشد؛
- CSP و سایر security headerها روی UI وجود داشته باشند.

---

## ۱۳. ترتیب فعال‌سازی

1. تغییر default Starter به opt-in پس از explicit config همه مصرف‌کنندگان؛
2. ایجاد Docs Security chain مشترک و تست 401/403/404؛
3. افزودن dependency Starter به UAA؛
4. بسته‌بندی فایل‌های UAA و Web در مسیرهای versioned؛
5. ثبت `API_DOC` سرویس رسید در Web؛
6. اصلاح link generation برای context/forwarded prefix؛
7. ایجاد Swagger UI مبتنی بر `docId` و حذف arbitrary URL؛
8. افزودن CI validation و Boot JAR content test؛
9. فعال‌سازی local/test؛
10. Pilot روی ingress داخلی و سپس تصمیم مستقل برای Production.

Rollback با `SCM_DOCS_ENABLED=false` و حذف route مستندات از ingress انجام می‌شود؛ سرویس‌های UAA و Web به کار خود ادامه می‌دهند.

---

## ۱۴. معیار Go-live

- Docs در Production بدون opt-in صریح در دسترس نباشد.
- هر مسیر Docs/Swagger داخل SecurityFilterChain و ingress policy مشخص باشد.
- Token فاقد `docs.read` نتواند سند را بخواند.
- UAA و Web فایل‌های MD/OpenAPI همان artifact را منتشر کنند.
- Swagger UI فقط `docId` ثبت‌شده و same-origin را باز کند.
- Contract نسخه‌دار overwrite نشود و drift در CI بررسی شود.
- خرابی Docs هیچ اثر availability روی Token Endpoint یا API رسید نداشته باشد.
- هیچ اطلاعات حساس یا Credential در سند، نمونه، Log و UI وجود نداشته باشد.
