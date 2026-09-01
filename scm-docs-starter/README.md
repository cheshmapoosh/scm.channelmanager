# راهنمای استفاده از `scm-docs-starter`

طراحی فعال‌سازی امن Markdown، OpenAPI و Swagger در `scm-uaa` و `scm-web` در [`UAA_WEB_DOCS_ACTIVATION_DESIGN.md`](UAA_WEB_DOCS_ACTIVATION_DESIGN.md) مستند شده است.

## هدف

ماژول `scm-docs-starter` یک کتابخانه‌ی مشترک برای انتشار مستندات داخلی SCM است. هر ماژول می‌تواند با اضافه‌کردن این dependency، مستندات خود را از طریق endpointهای استاندارد `/docs` و `/docs/api` منتشر کند.

این مستند توضیح می‌دهد:

1. `scm-docs-starter` چه کاری انجام می‌دهد.
2. ماژول‌ها چطور از آن استفاده کنند.
3. ورودی و خروجی APIهای آن چیست.
4. در `scm-web` مستندات API سرویس‌ها چطور پیاده‌سازی و ثبت شوند.
5. رکوردهای لازم برای اتصال مستندات به سرویس‌ها چطور درج شوند.

---

## 1. معماری کلی

`scm-docs-starter` سه نقش اصلی دارد:

### 1.1. Catalog

Catalog یعنی فهرست مستندات قابل نمایش یا دانلود.

برای تولید catalog، ماژول‌ها باید provider زیر را پیاده‌سازی کنند:

```java
public interface ScmDocCatalogProvider {
    Collection<ScmDocDescriptor> findAll();
}
```

خروجی این provider یک لیست از `ScmDocDescriptor` است.

### 1.2. Content

Content یعنی محتوای واقعی فایل مستند؛ مثلاً فایل Markdown، OpenAPI JSON، WSDL یا HTML.

برای تولید content، ماژول‌ها باید provider زیر را پیاده‌سازی کنند:

```java
public interface ScmDocContentProvider {
    Optional<ScmDocContent> findById(String docId);
}
```

اگر مستند با `docId` پیدا شود، محتوای آن برگردانده می‌شود. اگر پیدا نشود، خروجی `Optional.empty()` است.

### 1.3. Registry

`ScmDocsRegistry` همه‌ی providerهای موجود در Spring Context را جمع می‌کند.

رفتار Registry:

1. همه‌ی `ScmDocCatalogProvider`ها را صدا می‌زند.
2. همه‌ی `ScmDocDescriptor`ها را بر اساس `id` جمع می‌کند.
3. اگر `id` تکراری باشد، اولین descriptor نگه داشته می‌شود و مورد بعدی ignore می‌شود.
4. خروجی catalog را مرتب می‌کند.
5. برای دانلود محتوا، همه‌ی `ScmDocContentProvider`ها را به‌ترتیب صدا می‌زند تا provider مناسب پیدا شود.

---

## 2. فعال‌سازی در یک ماژول

### 2.1. اضافه‌کردن dependency

اگر ماژول داخل همین multi-module project است:

```gradle
dependencies {
    implementation project(':scm-docs-starter')
}
```

اگر بعداً `scm-docs-starter` به Maven repository داخلی publish شود:

```gradle
dependencies {
    implementation 'ir.daneshrefah.scm.docs:scm-docs-starter:1.0.0-SNAPSHOT'
}
```

---

## 3. تنظیمات `scm.docs`

`scm-docs-starter` از properties با prefix زیر استفاده می‌کند:

```yaml
scm:
  docs:
```

نمونه‌ی کامل:

```yaml
scm:
  docs:
    enabled: true
    base-path: /docs
    module-code: scm-provider-hps-shetab
    title:
      fa: مستندات ماژول شتاب
      en: Shetab Provider Documentation
    classpath-root: scm-docs
    api:
      enabled: true
      fail-fast: false
    documents:
      - id: scm-provider-hps-shetab.card-inquiry-guide
        module-code: scm-provider-hps-shetab
        service-code: card-inquiry
        version: v1
        category: GUIDE
        type: MARKDOWN
        title:
          fa: راهنمای سرویس استعلام کارت
          en: Card Inquiry Guide
        description:
          fa: راهنمای استفاده و نکات فنی سرویس استعلام کارت
          en: Usage guide and technical notes for card inquiry
        media-type: text/markdown; charset=UTF-8
        file-name: card-inquiry-guide.md
        classpath-location: card/card-inquiry-guide.md
        order: 10
```

### 3.1. توضیح propertyها

| Property | پیش‌فرض | توضیح |
|---|---:|---|
| `scm.docs.enabled` | `true` | فعال یا غیرفعال‌کردن مستندات |
| `scm.docs.base-path` | `/docs` | مسیر پایه‌ی controller |
| `scm.docs.module-code` | `scm` | کد ماژولی که مستندات را منتشر می‌کند |
| `scm.docs.title` | `SCM Documentation` | عنوان صفحه HTML مستندات |
| `scm.docs.classpath-root` | `scm-docs` | ریشه‌ی فایل‌های مستند در classpath |
| `scm.docs.api.enabled` | `true` | فعال یا غیرفعال‌کردن catalog مستندات API runtime |
| `scm.docs.api.fail-fast` | `false` | اگر `true` باشد، خطای تعریف API_DOC باعث exception می‌شود؛ اگر `false` باشد، مورد خراب warn و skip می‌شود |
| `scm.docs.documents` | خالی | لیست مستندات static قابل انتشار |

`scm-docs-starter` خودش cache policy، cache implementation و dependency به `scm-cache-starter` ندارد. تنظیمات `scm.docs.api.*` فقط رفتار API docs را کنترل می‌کنند. اگر provider یک ماژول به cache نیاز داشته باشد، cache باید در همان ماژول و با زیرساخت همان runtime تعریف شود.

---

## 4. ساختار فایل‌های مستند در classpath

اگر مقدار زیر تنظیم شده باشد:

```yaml
scm:
  docs:
    classpath-root: scm-docs
```

و یک document این مقدار را داشته باشد:

```yaml
classpath-location: card/card-inquiry-guide.md
```

فایل باید در مسیر زیر قرار بگیرد:

```text
src/main/resources/scm-docs/card/card-inquiry-guide.md
```

اگر `classpath-location` خالی باشد، مسیر فایل از روی `id` و extension پیش‌فرض `type` ساخته می‌شود.

مثلاً:

```yaml
id: my-guide
type: MARKDOWN
```

مسیر پیش‌فرض:

```text
src/main/resources/scm-docs/my-guide.md
```

---

## 5. مدل `ScmDocDescriptor`

`ScmDocDescriptor` metadata یک مستند است.

```java
public record ScmDocDescriptor(
        String id,
        String moduleCode,
        String serviceCode,
        String version,
        ScmDocCategory category,
        ScmDocType type,
        Map<String, String> title,
        Map<String, String> description,
        String mediaType,
        String fileName,
        String href,
        int order
) {
}
```

### 5.1. توضیح فیلدها

| فیلد | توضیح |
|---|---|
| `id` | شناسه یکتای مستند. برای دانلود محتوا با همین مقدار استفاده می‌شود. |
| `moduleCode` | کد ماژولی که مستند را منتشر می‌کند؛ مثل `scm-web` یا `scm-provider-hps-shetab`. |
| `serviceCode` | اگر مستند مربوط به یک سرویس باشد، کد آن سرویس؛ مثل `card-inquiry`. |
| `version` | نسخه‌ی مستند یا API؛ مثل `v1`. |
| `category` | دسته‌بندی مستند؛ مثل `API`, `GUIDE`, `RELEASE_NOTE`. |
| `type` | نوع فایل؛ مثل `MARKDOWN`, `OPENAPI_JSON`, `WSDL`. |
| `title` | عنوان چندزبانه. کلیدها معمولاً `fa` و `en` هستند. |
| `description` | توضیح چندزبانه. |
| `mediaType` | نوع محتوا برای HTTP response. |
| `fileName` | نام فایل هنگام نمایش یا دانلود. |
| `href` | لینک دسترسی به محتوای مستند. |
| `order` | ترتیب نمایش در خروجی catalog و UI. |

### 5.2. تفاوت `moduleCode` و `serviceCode`

این دو فیلد نباید یکی شوند.

`moduleCode` مشخص می‌کند مستند توسط کدام ماژول منتشر شده است.

مثال:

```text
scm-web
scm-provider-hps-shetab
scm-cache
scm-uaa
```

`serviceCode` مشخص می‌کند مستند مربوط به کدام سرویس کسب‌وکاری است.

مثال:

```text
card-inquiry
card-transfer
account-balance
otp-request
```

یک مستند ممکن است module-level باشد و اصلاً `serviceCode` نداشته باشد. همچنین ممکن است چند ماژول برای یک `serviceCode` مستند داشته باشند.

مثال:

```text
moduleCode=scm-web
serviceCode=card-inquiry
type=OPENAPI_JSON

moduleCode=scm-provider-hps-shetab
serviceCode=card-inquiry
type=GUIDE
```

### 5.3. کاربرد `order`

`order` برای ترتیب نمایش مستندات است.

مثلاً برای یک سرویس:

```text
10  OpenAPI JSON
20  WSDL
30  راهنمای Markdown
40  Release note
```

اگر `order` مشخص نشود، مقدار پیش‌فرض `0` است.

---

## 6. نوع مستندات

`ScmDocType` نوع فایل مستند را مشخص می‌کند.

مقادیر فعلی:

```text
OPENAPI_JSON
WSDL
ISO8583_SCHEMA
MARKDOWN
HTML
```

جدول کاربرد:

| Type | Media Type پیش‌فرض | Extension پیش‌فرض | کاربرد |
|---|---|---|---|
| `OPENAPI_JSON` | `application/json` | `.json` | قرارداد REST/OpenAPI |
| `WSDL` | `application/xml` | `.wsdl` | قرارداد SOAP |
| `ISO8583_SCHEMA` | `application/json` | `.json` | schema پیام ISO8583 |
| `MARKDOWN` | `text/markdown; charset=UTF-8` | `.md` | راهنمای متنی |
| `HTML` | `text/html; charset=UTF-8` | `.html` | مستند HTML |

---

## 7. دسته‌بندی مستندات

`ScmDocCategory` دسته‌ی مستند را مشخص می‌کند.

مقادیر فعلی:

```text
API
GUIDE
CHANGE_MANAGEMENT
RELEASE_NOTE
PLATFORM
```

راهنمای انتخاب:

| Category | کاربرد |
|---|---|
| `API` | قرارداد API، OpenAPI، WSDL، schema |
| `GUIDE` | راهنمای استفاده یا پیاده‌سازی |
| `CHANGE_MANAGEMENT` | مستندات تغییرات، migration و breaking change |
| `RELEASE_NOTE` | یادداشت انتشار |
| `PLATFORM` | مستندات عمومی پلتفرم SCM |

---

## 8. APIهای `scm-docs-starter`

مسیر پایه به‌صورت پیش‌فرض `/docs` است. اگر `scm.docs.base-path` تغییر کند، همه‌ی مسیرها بر اساس مقدار جدید ساخته می‌شوند.

### 8.1. نمایش HTML مستندات

```http
GET /docs
Accept: text/html
```

خروجی:

```http
200 OK
Content-Type: text/html
```

این endpoint یک صفحه HTML ساده برای نمایش catalog مستندات برمی‌گرداند.

ورودی مهم:

| ورودی | توضیح |
|---|---|
| `Accept-Language` | برای انتخاب زبان عنوان و توضیحات در HTML استفاده می‌شود. |

نمونه:

```bash
curl -H "Accept-Language: fa" http://localhost:8080/docs
```

---

### 8.2. دریافت فهرست مستندات به‌صورت JSON

```http
GET /docs/api
Accept: application/json
```

خروجی فعلی فقط گروه‌های API docs را برمی‌گرداند:

```json
{
  "groups": [
    {
      "id": "scm-web.channel-mb.100.card-inquiry.v1",
      "moduleCode": "scm-web",
      "gatewayName": "channel.mb",
      "channelServiceAccessId": 100,
      "serviceCode": "card-inquiry",
      "version": "v1",
      "title": {
        "fa": "مستندات سرویس استعلام کارت",
        "en": "Card Inquiry API Docs"
      },
      "description": {
        "fa": "مستندات قراردادهای سرویس استعلام کارت",
        "en": "Card inquiry contract documents"
      },
      "documents": [
        {
          "id": "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
          "docType": "OPENAPI_JSON",
          "title": {
            "fa": "مستند OpenAPI سرویس استعلام کارت",
            "en": "Card Inquiry OpenAPI"
          },
          "description": {},
          "mediaType": "application/json",
          "fileName": "openapi.json",
          "href": "/docs/api/scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
          "order": 10
        }
      ],
      "order": 10
    }
  ]
}
```

در این خروجی کلید flat به نام `documents` وجود ندارد. هر آیتم داخل `groups[].documents[]` فقط `href` دارد و فیلدی به نام `downloadUrl` منتشر نمی‌شود.

نکته: صفحه HTML `/docs` همچنان می‌تواند مستندات عمومی و static را از catalog معمولی نمایش دهد، اما JSON endpoint `/docs/api` برای مستندات API runtime به‌صورت grouped طراحی شده است.

---

### 8.3. دریافت محتوای یک مستند

```http
GET /docs/api/{docId}
```

نمونه:

```http
GET /docs/api/scm-provider-hps-shetab.card-inquiry-guide
```

خروجی موفق:

```http
200 OK
Content-Type: text/markdown; charset=UTF-8
Content-Disposition: inline; filename="card-inquiry-guide.md"

# محتوای فایل
...
```

اگر `docId` وجود نداشته باشد:

```http
404 Not Found
```

اگر `docId` ناامن باشد، مثلاً شامل `/` یا `..` باشد:

```http
400 Bad Request
Content-Type: application/json

{
  "error": "Invalid documentation request"
}
```

---

## 9. استفاده از static docs در یک ماژول

این روش برای مستندات ثابت مثل guide، release note یا platform docs مناسب است.

### 9.1. ساخت فایل

```text
src/main/resources/scm-docs/card/card-inquiry-guide.md
```

محتوا:

```markdown
# راهنمای سرویس استعلام کارت

## هدف

این مستند نحوه استفاده از سرویس استعلام کارت را توضیح می‌دهد.
```

### 9.2. تنظیم `application.yml`

```yaml
scm:
  docs:
    enabled: true
    base-path: /docs
    module-code: scm-provider-hps-shetab
    title:
      fa: مستندات شتاب
      en: Shetab Documentation
    classpath-root: scm-docs
    documents:
      - id: scm-provider-hps-shetab.card-inquiry-guide
        service-code: card-inquiry
        version: v1
        category: GUIDE
        type: MARKDOWN
        title:
          fa: راهنمای سرویس استعلام کارت
          en: Card Inquiry Guide
        description:
          fa: راهنمای استفاده از سرویس استعلام کارت
          en: Usage guide for card inquiry service
        file-name: card-inquiry-guide.md
        classpath-location: card/card-inquiry-guide.md
        order: 10
```

### 9.3. تست

```bash
curl http://localhost:8080/docs
curl http://localhost:8080/docs/api/scm-provider-hps-shetab.card-inquiry-guide
```

نکته: مستندات static در صفحه HTML `/docs` دیده می‌شوند و محتوای آن‌ها با `/docs/api/{docId}` قابل دریافت است. خروجی JSON `/docs/api` مخصوص گروه‌های API docs runtime است.

---

## 10. استفاده از provider سفارشی

اگر مستندات از configuration ثابت نمی‌آیند و باید از دیتابیس، runtime یا منبع دیگری تولید شوند، یک provider سفارشی بسازید.

نمونه:

```java
@Component
public class MyModuleDocsProvider implements ScmDocCatalogProvider, ScmDocContentProvider {

    @Override
    public Collection<ScmDocDescriptor> findAll() {
        return List.of(new ScmDocDescriptor(
                "my-module.card-inquiry.openapi",
                "my-module",
                "card-inquiry",
                "v1",
                ScmDocCategory.API,
                ScmDocType.OPENAPI_JSON,
                Map.of("fa", "OpenAPI سرویس استعلام کارت", "en", "Card Inquiry OpenAPI"),
                Map.of("fa", "قرارداد REST سرویس", "en", "REST API contract"),
                "application/json",
                "openapi.json",
                null,
                10
        ));
    }

    @Override
    public Optional<ScmDocContent> findById(String docId) {
        if (!"my-module.card-inquiry.openapi".equals(docId)) {
            return Optional.empty();
        }

        ScmDocDescriptor descriptor = findAll().iterator().next();
        String body = """
                {
                  "openapi": "3.0.0",
                  "info": {
                    "title": "Card Inquiry",
                    "version": "v1"
                  }
                }
                """;

        return Optional.of(ScmDocContent.fromString(descriptor, body));
    }
}
```

---

## 11. پیاده‌سازی در `scm-web`

در `scm-web` هدف این است که مستندات API سرویس‌ها بر اساس اطلاعات runtime و رکوردهای دیتابیس منتشر شوند.

### 11.1. dependency

در `scm-web/build.gradle` باید dependency زیر وجود داشته باشد:

```gradle
implementation project(':scm-docs-starter')
```

### 11.2. provider runtime

در `scm-web` یک provider به نام زیر وجود دارد:

```text
ScmWebRuntimeApiDocCatalogProvider
```

این provider دو interface را پیاده‌سازی می‌کند:

```text
ScmApiDocGroupCatalogProvider
ScmDocContentProvider
```

وظیفه‌ی آن:

1. خواندن رکوردهای `ChannelServiceDefinitionEntity` با نوع `API_DOC`
2. خواندن `DefinitionEntity.details`
3. parse کردن JSON داخل `details` یا فایل معرفی‌شده در `detailsRef`
4. ساختن یک گروه API doc برای هر exposure
5. ساختن یک descriptor برای هر آیتم داخل `documents[]`
6. استفاده از Spring `CacheManager` برای cache کردن catalog metadata
7. load کردن فایل‌ها از classpath هنگام درخواست content

### 11.3. cache catalog در `scm-web`

`scm-web` به `scm-cache-starter` وابسته است و cache مربوط به catalog مستندات API را از طریق Spring `CacheManager` دریافت می‌کند. این cache متعلق به implementation در `scm-web` است و بخشی از `scm-docs-starter` نیست.

نام cache ثابت است:

```text
scm-web-api-doc-catalog
```

تنظیم پیشنهادی در `scm-web`:

```yaml
scm:
  cache:
    client:
      caches:
        scm-web-api-doc-catalog:
          type: local
          ttl: 60s
          maximum-size: 128
```

`ScmWebRuntimeApiDocCatalogProvider` به‌صورت optional از `ObjectProvider<CacheManager>` استفاده می‌کند. اگر `CacheManager` وجود نداشته باشد یا cache با نام `scm-web-api-doc-catalog` تعریف نشده باشد، provider با log ساخت‌یافته warning می‌دهد و بدون cache اجرا می‌شود.

cache key شامل این مقادیر است:

```text
runtimeMode
active runtime gatewayNames
failFast
apiDocsEnabled
```

فقط `RuntimeApiDocCatalog` cache می‌شود. محتوای فایل‌های مستند cache نمی‌شود و در زمان درخواست content از classpath خوانده می‌شود.

---

## 12. ساختار `Definition.details` برای API_DOC

برای یک سرویس می‌توان چند فایل مستند تعریف کرد.

نمونه:

```json
{
  "version": "v1",
  "documents": [
    {
      "docType": "OPENAPI_JSON",
      "name": "openapi.json",
      "title": {
        "fa": "مستند OpenAPI سرویس استعلام کارت",
        "en": "Card Inquiry OpenAPI"
      },
      "description": {
        "fa": "مستند قرارداد REST سرویس استعلام کارت",
        "en": "REST contract for card inquiry service"
      },
      "mediaType": "application/json",
      "source": {
        "type": "CLASSPATH",
        "path": "services/card/card-inquiry/v1/openapi/openapi.json"
      },
      "order": 10
    },
    {
      "docType": "WSDL",
      "name": "card-inquiry.wsdl",
      "title": {
        "fa": "WSDL سرویس استعلام کارت",
        "en": "Card Inquiry WSDL"
      },
      "mediaType": "application/xml",
      "source": {
        "type": "CLASSPATH",
        "path": "services/card/card-inquiry/v1/soap/card-inquiry.wsdl"
      },
      "order": 20
    },
    {
      "docType": "MARKDOWN",
      "name": "guide.md",
      "title": {
        "fa": "راهنمای فارسی سرویس استعلام کارت",
        "en": "Card Inquiry Guide"
      },
      "mediaType": "text/markdown; charset=UTF-8",
      "source": {
        "type": "CLASSPATH",
        "path": "services/card/card-inquiry/v1/markdown/guide.md"
      },
      "order": 30
    }
  ]
}
```

### 12.1. استفاده از `detailsRef` برای JSONهای بزرگ

اگر JSON مستندات بزرگ باشد، می‌توان داخل ستون `Definition.details` فقط یک reference نگه داشت:

```json
{
  "detailsRef": {
    "type": "CLASSPATH",
    "path": "services/card/card-inquiry/v1/api-docs.json"
  }
}
```

فایل خارجی باید همان schema کامل API_DOC را داشته باشد؛ یعنی شامل فیلدهایی مثل `version`, `title`, `description`, `order` و `documents` باشد:

```text
scm-web/src/main/resources/services/card/card-inquiry/v1/api-docs.json
```

در فاز فعلی، `detailsRef.type` فقط می‌تواند `CLASSPATH` باشد.

وقتی `detailsRef` استفاده می‌شود، `Definition.details` نباید هم‌زمان فیلدهای inline مربوط به API doc را هم داشته باشد. یعنی این فیلدها باید فقط داخل فایل external باشند:

```text
version
title
description
order
documents
```

در صورت ترکیب `detailsRef` با این فیلدها، خطای validation باید این متن را داشته باشد:

```text
API_DOC detailsRef must not be combined with inline API doc fields: version, title, description, order, documents
```

### 12.2. توضیح فیلدهای `details`

| فیلد | اجباری | توضیح |
|---|---:|---|
| `version` | خیر | نسخه API یا مستند. اگر نباشد، `unversioned` در نظر گرفته می‌شود. |
| `detailsRef.type` | شرطی | اگر `detailsRef` استفاده شود، در فاز فعلی فقط `CLASSPATH` پشتیبانی می‌شود. |
| `detailsRef.path` | شرطی | مسیر فایل JSON خارجی در classpath. باید زیر `services/` باشد. |
| `documents` | بله | لیست فایل‌های مستند مربوط به این API_DOC. در حالت `detailsRef`، این فیلد داخل فایل خارجی اجباری است. |
| `documents[].docType` | بله | یکی از مقادیر `ScmDocType`. |
| `documents[].name` | بله | نام منطقی فایل. در ساخت doc id و fileName استفاده می‌شود. |
| `documents[].title` | خیر | عنوان چندزبانه. |
| `documents[].description` | خیر | توضیح چندزبانه. |
| `documents[].mediaType` | خیر | اگر خالی باشد، از مقدار پیش‌فرض `docType` استفاده می‌شود. |
| `documents[].source.type` | بله | در فاز فعلی فقط `CLASSPATH` پشتیبانی می‌شود. |
| `documents[].source.path` | بله | مسیر فایل در classpath. باید زیر `services/` باشد. |
| `documents[].order` | خیر | ترتیب نمایش داخل catalog. |

### 12.3. قوانین validation مسیر

برای `detailsRef.path` و `documents[].source.path` این قوانین اعمال می‌شود:

1. مسیر باید relative باشد.
2. مسیر باید با `services/` شروع شود.
3. مسیر نباید با `scm-docs/` شروع شود.
4. مسیر نباید شامل path traversal مثل `..` یا segment خالی باشد.
5. مسیر نباید absolute، شامل null byte یا شامل `//` باشد.

همچنین JSON نهایی API_DOC باید `documents[]` داشته باشد و این array نباید خالی باشد. هر آیتم داخل `documents[]` باید حداقل `docType`, `name`, `source.type` و `source.path` داشته باشد.

---

## 13. محل فایل‌های API docs در `scm-web`

برای example بالا، فایل‌ها باید در این مسیرها قرار بگیرند:

```text
scm-web/src/main/resources/services/card/card-inquiry/v1/openapi/openapi.json
scm-web/src/main/resources/services/card/card-inquiry/v1/soap/card-inquiry.wsdl
scm-web/src/main/resources/services/card/card-inquiry/v1/markdown/guide.md
```

قاعده پیشنهادی مسیر:

```text
services/{domain}/{service-code}/{version}/{doc-kind}/{file-name}
```

نمونه:

```text
services/card/card-inquiry/v1/openapi/openapi.json
services/card/card-inquiry/v1/soap/card-inquiry.wsdl
services/card/card-inquiry/v1/markdown/guide.md
```

---

## 14. درج رکوردهای لازم برای مستند API سرویس در `scm-web`

برای اینکه مستندات یک سرویس در `/docs/api` دیده شود، باید این ارتباط برقرار شود:

```text
GatewayChannel
        +
ChannelServiceAccess
        +
Definition(details)
        =
ChannelServiceDefinition(type=API_DOC)
```

### 14.1. پیش‌نیازها

قبل از درج API_DOC، این موارد باید وجود داشته باشند:

1. سرویس در جدول سرویس‌ها ثبت شده باشد.
2. کانال یا service-domain مربوطه وجود داشته باشد.
3. `GatewayChannelEntity` وجود داشته باشد.
4. `ChannelServiceAccessEntity` بین channel و service وجود داشته باشد.
5. فایل‌های مستند در classpath `scm-web` قرار گرفته باشند.

### 14.2. درج `DefinitionEntity`

`DefinitionEntity` در جدول زیر ذخیره می‌شود:

```text
REF.TBL_SCM_DEFINITION
```

فیلد مهم برای API_DOC:

```text
DETAILS
```

نمونه SQL مفهومی:

```text
INSERT INTO REF.TBL_SCM_DEFINITION (
    DEFINITION_ID,
    NAME,
    TITLE,
    DETAILS
) VALUES (
    'def-card-inquiry-api-doc-v1',
    'card-inquiry-api-doc-v1',
    'Card Inquiry API Documentation v1',
    '{
      "version": "v1",
      "documents": [
        {
          "docType": "OPENAPI_JSON",
          "name": "openapi.json",
          "title": {
            "fa": "مستند OpenAPI سرویس استعلام کارت",
            "en": "Card Inquiry OpenAPI"
          },
          "mediaType": "application/json",
          "source": {
            "type": "CLASSPATH",
            "path": "services/card/card-inquiry/v1/openapi/openapi.json"
          },
          "order": 10
        }
      ]
    }'
);
```

اگر JSON کامل بزرگ است، مقدار `DETAILS` می‌تواند فقط به فایل خارجی اشاره کند:

```text
INSERT INTO REF.TBL_SCM_DEFINITION (
    DEFINITION_ID,
    NAME,
    TITLE,
    DETAILS
) VALUES (
    'def-card-inquiry-api-doc-v1',
    'card-inquiry-api-doc-v1',
    'Card Inquiry API Documentation v1',
    '{
      "detailsRef": {
        "type": "CLASSPATH",
        "path": "services/card/card-inquiry/v1/api-docs.json"
      }
    }'
);
```

نکته مهم: اگر از inline JSON استفاده شود، طول `details` در `DefinitionEntity` باید برای چند مستند با title و description چندزبانه کافی باشد. برای JSONهای طولانی، `detailsRef` روش پیشنهادی است.

### 14.3. درج `ChannelServiceDefinitionEntity`

`ChannelServiceDefinitionEntity` در جدول زیر ذخیره می‌شود:

```text
REF.TBL_SCM_CHN_SVC_DEFINITION
```

برای API_DOC مقدار `TYPE` باید `API_DOC` باشد.

نمونه SQL مفهومی:

```text
INSERT INTO REF.TBL_SCM_CHN_SVC_DEFINITION (
    CHN_SVC_DEFINITION_ID,
    CHANNEL_SERVICE_ACCESS_ID,
    GATEWAY_CHANNEL_ID,
    TYPE,
    DEFINITION_ID
) VALUES (
    'chn-svc-def-card-inquiry-api-doc-v1',
    100,
    'gateway-channel-mb',
    'API_DOC',
    'def-card-inquiry-api-doc-v1'
);
```

توضیح فیلدها:

| فیلد | توضیح |
|---|---|
| `CHANNEL_SERVICE_ACCESS_ID` | شناسه دسترسی channel به service |
| `GATEWAY_CHANNEL_ID` | شناسه gateway/channel یا domain runtime |
| `TYPE` | برای مستند API باید `API_DOC` باشد |
| `DEFINITION_ID` | شناسه رکورد `DefinitionEntity` که JSON مستندات در آن است |

برای چند artifact مستندات یک سرویس، چند ردیف `API_DOC` نساز. برای هر ترکیب gateway/channel-service-access/service exposure حداکثر یک ردیف `API_DOC` باید وجود داشته باشد. همان یک ردیف به یک `DefinitionEntity` اشاره می‌کند و همه artifactها داخل `details.documents[]` یا فایل معرفی‌شده با `detailsRef` تعریف می‌شوند.

---

## 15. خروجی grouped در `/docs/api`

با توجه به اینکه یک `API_DOC` می‌تواند چند فایل مستند داشته باشد، خروجی `/docs/api` فقط به‌صورت grouped منتشر می‌شود.

نمونه خروجی:

```json
{
  "groups": [
    {
      "id": "scm-web.channel-mb.100.card-inquiry.v1",
      "moduleCode": "scm-web",
      "gatewayName": "channel.mb",
      "channelServiceAccessId": 100,
      "serviceCode": "card-inquiry",
      "version": "v1",
      "title": {
        "fa": "مستندات سرویس استعلام کارت",
        "en": "Card Inquiry API Documentation"
      },
      "documents": [
        {
          "id": "scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
          "docType": "OPENAPI_JSON",
          "fileName": "openapi.json",
          "mediaType": "application/json",
          "href": "/docs/api/scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json",
          "order": 10
        },
        {
          "id": "scm-web.channel-mb.100.card-inquiry.v1.WSDL.card-inquiry-wsdl",
          "docType": "WSDL",
          "fileName": "card-inquiry.wsdl",
          "mediaType": "application/xml",
          "href": "/docs/api/scm-web.channel-mb.100.card-inquiry.v1.WSDL.card-inquiry-wsdl",
          "order": 20
        }
      ]
    }
  ]
}
```

در این مدل:

1. `groups[].id` شناسه گروه مستندات API است.
2. `groups[].documents[].id` شناسه فایل قابل دریافت است.
3. دانلود محتوا همچنان با `documents[].id` انجام می‌شود.
4. UI می‌تواند همه مستندات یک سرویس را زیر یک card یا section نمایش دهد.
5. فیلد `downloadUrl` وجود ندارد؛ لینک دریافت محتوا فقط در `href` قرار می‌گیرد.

---

## 16. رفتار پیشنهادی برای نبود API_DOC

مستندات API نباید اجرای سرویس را متوقف کند.

رفتار پیشنهادی:

| وضعیت | رفتار پیشنهادی |
|---|---|
| `API_DOC` وجود ندارد | route سرویس ساخته شود، فقط docs برای آن سرویس خالی باشد |
| `API_DOC` وجود دارد ولی JSON خراب است | اگر fail-fast خاموش است، warn و skip شود |
| `detailsRef` وجود دارد ولی فایل آن پیدا نمی‌شود یا JSON آن خراب است | اگر fail-fast خاموش است، warn و skip شود |
| فایل classpath وجود ندارد | content endpoint برای همان doc باید `404` بدهد |
| `API_DOC` خارج از runtime فعلی است | نادیده گرفته شود |

تنظیم پیشنهادی:

```yaml
scm:
  docs:
    api:
      enabled: true
      fail-fast: false
```

پیش‌فرض‌ها:

```text
enabled=true
fail-fast=false
```

برای `scm-web`، cache catalog از مسیر `scm-cache-starter` و Spring `CacheManager` تنظیم می‌شود:

```yaml
scm:
  cache:
    client:
      caches:
        scm-web-api-doc-catalog:
          type: local
          ttl: 60s
          maximum-size: 128
```

این cache فقط catalog metadata و mapping بین `docId` و مسیر classpath را نگه می‌دارد. محتوای فایل‌ها cache نمی‌شود. اگر `fail-fast=true` باشد و catalog نامعتبر exception بدهد، آن exception در cache ذخیره نمی‌شود. اگر `fail-fast=false` باشد، catalog نهایی با skip شدن موارد خراب می‌تواند cache شود.

---

## 17. چک‌لیست افزودن مستند API برای یک سرویس

برای اضافه‌کردن مستند API یک سرویس در `scm-web`:

1. فایل‌های مستند را در `src/main/resources/services/...` قرار بده.
2. JSON مربوط به API_DOC را آماده کن؛ برای JSONهای کوچک inline داخل `Definition.details` و برای JSONهای بزرگ در فایل external با `detailsRef`.
3. `DefinitionEntity` را با `details` یا `detailsRef` ثبت کن.
4. فقط یک `ChannelServiceDefinitionEntity` با `TYPE='API_DOC'` برای همان gateway/channel-service-access/service exposure ثبت کن.
5. مطمئن شو `CHANNEL_SERVICE_ACCESS_ID` و `GATEWAY_CHANNEL_ID` درست هستند.
6. برنامه را اجرا کن.
7. خروجی catalog را تست کن:

```bash
curl http://localhost:8080/docs/api
```

8. محتوای یک فایل را تست کن:

```bash
curl http://localhost:8080/docs/api/{docId}
```

---

## 18. خطاهای رایج

### 18.1. مستند در `/docs/api` دیده نمی‌شود

بررسی کن:

1. `scm.docs.enabled=true` باشد.
2. برای مستندات API runtime، `scm.docs.api.enabled=true` باشد.
3. ماژول `scm-docs-starter` dependency شده باشد.
4. provider مورد نظر Spring bean شده باشد.
5. `id` مستند خالی یا تکراری نباشد.
6. برای `scm-web` رکورد `ChannelServiceDefinitionEntity` با `TYPE=API_DOC` وجود داشته باشد.
7. مسیرهای `detailsRef.path` و `documents[].source.path` زیر `services/` باشند و path traversal نداشته باشند.

### 18.2. دانلود مستند `404` می‌دهد

بررسی کن:

1. `docId` دقیقاً با مقدار catalog یکی باشد.
2. فایل در classpath وجود داشته باشد.
3. مسیر `source.path` یا `classpath-location` درست باشد.
4. فایل داخل `src/main/resources` قرار گرفته باشد.

### 18.3. خطای `400 Bad Request`

احتمالاً `docId` ناامن است؛ مثلاً شامل `/`, `\`, `..` یا null byte است.

### 18.4. فقط یکی از مستندات تکراری دیده می‌شود

اگر چند provider یک `id` یکسان برگردانند، Registry اولین مورد را نگه می‌دارد و موارد بعدی را ignore می‌کند. پس `id` باید در کل سیستم یکتا باشد.

---

## 19. قواعد نام‌گذاری پیشنهادی

برای مستندات static ماژول‌ها:

```text
{moduleCode}.{serviceCode}.{version}.{docType}.{name}
```

مثال:

```text
scm-provider-hps-shetab.card-inquiry.v1.MARKDOWN.guide
```

برای مستندات runtime در `scm-web`:

```text
{moduleCode}.{gatewayName}.{channelServiceAccessId}.{serviceCode}.{version}.{docType}.{name}
```

مثال:

```text
scm-web.channel-mb.100.card-inquiry.v1.OPENAPI_JSON.openapi-json
```

نکته: چون `gatewayName` ممکن است شامل نقطه باشد، برای ساخت `id` بهتر است normalize شود:

```text
channel.mb -> channel-mb
```

---

## 20. توصیه‌های مهم

1. `id` مستند را کوتاه ولی یکتا انتخاب کن.
2. در `id` از `/`, `\`, `..` استفاده نکن.
3. برای متن‌های قابل نمایش، `title` و `description` را چندزبانه ثبت کن.
4. `moduleCode` و `serviceCode` را یکی نکن؛ این دو مفهوم متفاوت دارند.
5. برای ترتیب نمایش، `order` را با فاصله 10تایی تنظیم کن تا بعداً بتوان آیتم جدید بین آن‌ها اضافه کرد.
6. محتوای فایل‌های حساس، token، password، شماره کارت، PIN، OTP یا داده واقعی مشتری را داخل مستندات commit نکن.
7. برای API_DOCهای طولانی، ستون `Definition.details` باید از نظر طول بررسی شود.
8. نبود مستند API نباید باعث fail شدن route سرویس شود.
