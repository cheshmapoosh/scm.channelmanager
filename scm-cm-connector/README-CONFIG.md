# راهنمای تنظیمات scm-cm-connector

## 1. هدف این فایل

| کلید | عنوان | توضیحات |
|---|---|---|
| `scm-cm-connector` | هدف راهنما | این فایل برای پشتیبانی توضیح میدهد تنظیمات connector، security، cache client و observation چگونه مقداردهی میشوند. |
| `application.yml` | نقشه اصلی | propertyهای واقعی سرویس در این فایل تعریف میشوند و مقدار آنها از متغیرها خوانده میشود. |

## 2. قانون کلی تنظیمات

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev,test,pilot,prod` | profileهای مجاز | فقط این چهار profile مجاز هستند و profileهای قدیمی نباید فعال شوند. |
| `SCM_UAA_BASE_URL` | آدرس واحد UAA | issuer و JWK سرویس از همین متغیر ساخته میشوند. |
| `متغیر جداگانه issuer` | متغیر غیرمجاز | نباید در profileها تعریف شود، چون issuer از `SCM_UAA_BASE_URL` خوانده میشود. |
| `متغیر جداگانه JWK` | متغیر غیرمجاز | نباید در profileها تعریف شود، چون JWK از `SCM_UAA_BASE_URL/oauth2/jwks` ساخته میشود. |
| `SCM_*` | منبع مقداردهی | profileها و Config Server باید فقط متغیر flat تعریف کنند و propertyهای nested را تکرار نکنند. |

## 3. فایلهای مهم

| کلید | عنوان | توضیحات |
|---|---|---|
| `src/main/resources/application.yml` | فایل اصلی تنظیمات | mapper مرکزی سرویس است و security، cache client و connector config را از متغیرها میخواند. |
| `src/main/resources/application-dev.yml` | تنظیمات dev | Config Server را خاموش میکند و مقدارهای local/dev را تعریف میکند. |
| `src/main/resources/application-test.yml` | تنظیمات test | فقط Config Server را bootstrap میکند و مقدارها باید از Config Server بیایند. |
| `src/main/resources/application-pilot.yml` | تنظیمات pilot | فقط Config Server را bootstrap میکند و نبود Config Server باید startup را شکست دهد. |
| `src/main/resources/application-prod.yml` | تنظیمات prod | فقط Config Server را bootstrap میکند و مقدارهای حساس باید از Secret Management بیایند. |
| `scm-config/config-repo/scm-cm-connector/application-*.yml` | فایلهای متغیر Config Server | متغیرهای محیطی connector مثل UAA و cache را به صورت flat نگه میدارند. |

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای سرویس است. مقدار اشتباه باعث label اشتباه در log و trace میشود. |
| `SCM_APP` | نام برنامه | نام Spring application است و معمولاً باید `scm-cm-connector` باشد. |
| `SCM_METADATA_NAMESPACE` | namespace اجرا | در Kubernetes از metadata.namespace میآید و در dev مقدار local دارد. |
| `SCM_METADATA_INSTANCE_ID` | شناسه نمونه | شناسه node یا pod است و برای عیب یابی استفاده میشود. |
| `SCM_METADATA_TIME_ZONE` | timezone فایل | اگر خالی باشد timezone سیستم JVM استفاده میشود و فقط برای نام فایلهای observation است. |
| `SCM_SERVER_PORT` | پورت سرویس | پورت HTTP سرویس است. مقدار اشتباه باعث شکست route یا health check میشود. |
| `SCM_UAA_BASE_URL` | آدرس UAA | آدرس issuer و JWK از همین مقدار ساخته میشود. مقدار اشتباه باعث شکست اعتبارسنجی JWT میشود. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر کش | نام کلاستر cache client است و باید با محیط هماهنگ باشد. |
| `SCM_CACHE_ADDRESS` | آدرس کش | آدرس cache server است. مقدار اشتباه باعث کار نکردن cache session میشود. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_RESOURCE_SERVER_ENABLED` | فعال بودن resource server | اگر false شود اعتبارسنجی JWT طبق config خاموش میشود. تغییر اشتباه ریسک امنیتی دارد. |
| `SCM_RESOURCE_SERVER_METHOD_SECURITY_ENABLED` | method security | کنترل method-level security است. مقدار اشتباه ممکن است دسترسیها را نادرست کند. |
| `SCM_CM_CONNECTOR_RESOURCE_AUDIENCE` | audience مورد انتظار | audience مورد انتظار token است. مقدار اشتباه باعث reject شدن token معتبر میشود. |
| `SCM_CM_CONNECTOR_REQUIRED_CLAIM_SUB` | claim کاربر | نام claim اجباری subject است. تغییر اشتباه باعث رد شدن requestها میشود. |
| `SCM_CM_CONNECTOR_REQUIRED_CLAIM_SESSION` | claim session | نام claim session است. مقدار اشتباه باعث reject شدن token میشود. |
| `SCM_CM_CONNECTOR_ADMIN_CACHE_ENDPOINTS_ENABLED` | endpointهای admin cache | دسترسی endpointهای مدیریتی cache را کنترل میکند. در prod باید طبق سیاست امنیتی تنظیم شود. |
| `SCM_CACHE_DISTRIBUTED` | cache distributed | روشن یا خاموش بودن cache distributed client است. مقدار اشتباه روی session cache اثر میگذارد. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر اصلی observation | مسیر پایه log، trace و audit است. مقدار اشتباه باعث نوشته نشدن فایلها در مسیر مورد انتظار میشود. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | لاگ کنسول | فقط در profile `dev` باید روشن باشد. در test، pilot و prod خاموش است. |
| `SCM_OBS_LOG_FILE_ENABLED` | لاگ فایل | فایل log را روشن یا خاموش میکند. خاموش بودن اشتباه باعث از دست رفتن لاگ عملیاتی میشود. |
| `SCM_OBS_TRACE_FILE_ENABLED` | فایل trace | فایل trace را کنترل میکند و برای عیب یابی requestها مفید است. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit را کنترل میکند و باید جدا از application log باقی بماند. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | آدرس دریافت تنظیمات مرکزی است. مقدار اشتباه در test/pilot/prod مانع startup درست میشود. |
| `SCM_LABEL` | label کانفیگ | label مربوط به Spring Cloud Config است و میتواند branch، tag یا commit باشد. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری Config Server است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. باید از Secret یا محیط امن تأمین شود. |
| `SCM_CONFIG_FAIL_FAST` | شکست سریع | در pilot/prod باید فعال باشد تا سرویس با config ناقص بالا نیاید. |

## 8. مواردی که نباید دوباره استفاده شوند

| کلید | عنوان | توضیحات |
|---|---|---|
| `ib4dev,ib4test,scm4dev,scm4test,mb4dev,mb4test,pwa4dev,pwa4test` | profileهای قدیمی | این profileها نباید برای CM connector فعال شوند. |
| `متغیر جداگانه issuer` | متغیر حذف شده | نباید در profileها تعریف شود. issuer از `SCM_UAA_BASE_URL` ساخته میشود. |
| `متغیر جداگانه JWK` | متغیر حذف شده | نباید در profileها تعریف شود. JWK از `SCM_UAA_BASE_URL/oauth2/jwks` ساخته میشود. |
| `نام مبهم cache` | نام غیرمجاز | نباید به عنوان نام کلاستر cache استفاده شود. مقدار درست dev برابر `scm-cache-dev` است. |
| `scm.log.app.*` | تنظیم قدیمی logback | مسیرهای log باید از observation و `SCM_OBS_ROOT_DIR` ساخته شوند. |

## 9. نکات خاص هر محیط

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev` | محیط توسعه | Config Server خاموش است و `SCM_UAA_BASE_URL` از `application-dev.yml` خوانده میشود. |
| `test` | محیط تست | مقدار `SCM_UAA_BASE_URL` باید آدرس UAA تست باشد و cache cluster باید `scm-cache-test` باشد. |
| `pilot` | محیط پایلوت | مقدارهای حساس باید از Secret یا تنظیمات امن محیط بیایند و نباید hardcode شوند. |
| `prod` | محیط عملیاتی | اگر Config Server یا Secretها در دسترس نباشند، سرویس نباید با config ناقص بالا بیاید. |
