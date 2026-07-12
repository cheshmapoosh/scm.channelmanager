# راهنمای تنظیمات scm-config

## 1. هدف این فایل

| کلید | عنوان | توضیحات |
|---|---|---|
| `scm-config` | هدف راهنما | این فایل برای پشتیبانی توضیح میدهد Config Server چگونه آدرس repo، امنیت، encryption و mapping برنامه ها را از متغیرها میخواند. |
| `Config Server` | نقش سرویس | این سرویس خودش نباید از Config Server دیگری config بگیرد و تنظیماتش local و variable-based است. |

## 2. قانون کلی تنظیمات

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev,test,pilot,prod` | profileهای مجاز | mapping برنامه ها باید فقط این چهار profile را نشان دهد. |
| `SCM_CONFIG_*` | متغیرهای Config Server | تنظیمات repo، امنیت، prefix، encryption و mapping از این متغیرها تأمین میشود. |
| `Credential` | مقدار حساس | passwordها، کلید encryption و credentialهای repo حساس هستند و نباید در ticket، screenshot یا chat ارسال شوند. |
| `spring.cloud.config.enabled=false` | قانون self-fetch | این سرویس نباید به عنوان Config Client از خودش config بگیرد. |

## 3. فایلهای مهم

| کلید | عنوان | توضیحات |
|---|---|---|
| `src/main/resources/application.yml` | فایل اصلی تنظیمات | mapper مرکزی Config Server است و propertyهای واقعی را از متغیرها میخواند. |
| `src/main/resources/application-dev.yml` | تنظیمات dev | مقدارهای local/dev را تعریف میکند و Config Client را فعال نمیکند. |
| `src/main/resources/application-test.yml` | تنظیمات test | مقدارهای test خود Config Server را local و variable-based نگه میدارد. |
| `src/main/resources/application-pilot.yml` | تنظیمات pilot | مقدارهای pilot را local نگه میدارد و secretها باید از محیط امن بیایند. |
| `src/main/resources/application-prod.yml` | تنظیمات prod | مقدارهای prod را local نگه میدارد و passwordها و encryption key نباید hardcode شوند. |
| مخزن خارجی Spring Cloud Config | تنظیمات سرویس‌ها | خارج از این source tree مدیریت می‌شود و propertyهای هر محیط را از repository و runtime امن در اختیار Config Server می‌گذارد. |

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای Config Server است. مقدار اشتباه باعث label اشتباه در log و trace میشود. |
| `SCM_APP` | نام برنامه | نام Spring application است و معمولاً باید `scm-config` باشد. |
| `SCM_METADATA_NAMESPACE` | namespace اجرا | در Kubernetes از metadata.namespace میآید و در dev مقدار local دارد. |
| `SCM_METADATA_INSTANCE_ID` | شناسه نمونه | شناسه node یا pod است و برای عیب یابی استفاده میشود. |
| `SCM_METADATA_TIME_ZONE` | timezone فایل | اگر خالی باشد timezone سیستم JVM استفاده میشود و فقط برای نام فایلهای observation است. |
| `SCM_CONFIG_SERVER_PORT` | پورت Config Server | پورت HTTP سرویس است. مقدار اشتباه باعث شکست دسترسی clientها میشود. |
| `SCM_CONFIG_SERVER_PREFIX` | prefix endpoint | مسیر endpoint Config Server است و باید با clientها هماهنگ باشد. |
| `SCM_CONFIG_LOG_PATH` | مسیر log قدیمی Spring | مسیر log file مربوط به Spring logging است. برای observation از `SCM_OBS_ROOT_DIR` استفاده میشود. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_REPO_URI` | آدرس config repo | آدرس git یا مسیر repo تنظیمات است. مقدار اشتباه باعث نشدن دریافت config توسط clientها میشود. |
| `SCM_CONFIG_REPO_SEARCH_PATHS` | مسیر جستجوی repo | مسیر جستجو برای applicationها است. مقدار معمول `{application}` است. |
| `SCM_LABEL` | label کانفیگ | label مربوط به Spring Cloud Config است و میتواند branch، tag یا commit باشد. |
| `SCM_CONFIG_REPO_BASEDIR` | مسیر checkout | مسیر local checkout repo است. اگر دسترسی یا فضا مشکل داشته باشد Config Server خطا میدهد. |
| `SCM_CONFIG_REPO_USERNAME` | کاربر repo | نام کاربری repo است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_REPO_PASSWORD` | رمز repo | رمز repo است و کاملاً حساس است. باید از Secret یا محیط امن بیاید. |
| `SCM_CONFIG_USERNAME` | کاربر سرویس config | کاربر اصلی دسترسی به Config Server است و حساس محسوب میشود. |
| `SCM_CONFIG_PASSWORD` | رمز سرویس config | رمز اصلی Config Server است و کاملاً حساس است. |
| `SCM_CONFIG_ENCRYPT_KEY` | کلید encryption | کلید رمزنگاری config است و کاملاً حساس است. نباید hardcode یا در ticket ارسال شود. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر اصلی observation | مسیر پایه log، trace و audit است. مقدار اشتباه باعث نوشته نشدن فایلها در مسیر مورد انتظار میشود. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | لاگ کنسول | لاگ کنسول را روشن یا خاموش میکند. در dev معمولاً روشن است. |
| `SCM_OBS_LOG_FILE_ENABLED` | لاگ فایل | فایل log را روشن یا خاموش میکند. خاموش بودن اشتباه باعث از دست رفتن لاگ عملیاتی میشود. |
| `SCM_OBS_TRACE_FILE_ENABLED` | فایل trace | فایل trace را کنترل میکند و برای عیب یابی requestهای config مفید است. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit را کنترل میکند و باید جدا از application log باقی بماند. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `spring.cloud.config.enabled` | عدم اتصال client | برای خود `scm-config` باید `false` باشد. این سرویس نباید از خودش config دریافت کند. |
| `SCM_CONFIG_ADMIN_USERNAME` | کاربر admin local | کاربر local admin است و حساس محسوب میشود. |
| `SCM_CONFIG_ADMIN_PASSWORD` | رمز admin local | رمز local admin است و کاملاً حساس است. باید از Secret یا محیط امن بیاید. |
| `SCM_CONFIG_CFG_USERNAME` | کاربر config local | کاربر local با نقش CONFIG است و برای دسترسی به endpointهای config استفاده میشود. |
| `SCM_CONFIG_CFG_PASSWORD` | رمز config local | رمز کاربر CONFIG است و کاملاً حساس است. |

## 8. مواردی که نباید دوباره استفاده شوند

| کلید | عنوان | توضیحات |
|---|---|---|
| `ib4dev,ib4test,scm4dev,scm4test,mb4dev,mb4test,pwa4dev,pwa4test` | profileهای قدیمی | این profileها نباید در mapping برنامه ها برگردند. |
| `optional:configserver:` | import اختیاری | برای clientهای pilot/prod نباید استفاده شود. نبود Config Server باید startup را شکست دهد. |
| `متغیر جداگانه issuer` | متغیر حذف شده | در فایلهای config repo نباید تعریف شود. سرویسها باید از `SCM_UAA_BASE_URL` استفاده کنند. |
| `متغیر جداگانه JWK` | متغیر حذف شده | در فایلهای config repo نباید تعریف شود. JWK از `SCM_UAA_BASE_URL/oauth2/jwks` ساخته میشود. |
| `فایل compose قدیمی` | فایل حذف شده | فایل compose قدیمی از repository حذف شده و نباید دوباره برای اصلاح port یا jar path برگردانده شود. |

## 9. نکات خاص هر محیط

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev` | محیط توسعه | مقدارهای local از `application-dev.yml` میآیند و passwordهای نمونه فقط برای local هستند. |
| `test` | محیط تست | repo و mapping باید profileهای `dev,test,pilot,prod` را نگه دارند و secretها از محیط امن بیایند. |
| `pilot` | محیط پایلوت | encryption key و passwordها نباید hardcode شوند و باید از Secret Management تأمین شوند. |
| `prod` | محیط عملیاتی | نبود repo، password یا encryption key معتبر باید مانع سرویسدهی config ناقص شود. |

## 10. mapping برنامه ها

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_APPLICATIONS_SCM_WEB` | profileهای scm-web | باید فقط `dev, test, pilot, prod` باشد مگر با تغییر رسمی profileها. |
| `SCM_CONFIG_APPLICATIONS_SCM_CACHE` | profileهای scm-cache | باید فقط `dev, test, pilot, prod` باشد. |
| `SCM_CONFIG_APPLICATIONS_SCM_UAA` | profileهای scm-uaa | باید فقط `dev, test, pilot, prod` باشد. |
| `SCM_CONFIG_APPLICATIONS_SCM_LOGGING` | profileهای scm-logging | باید فقط `dev, test, pilot, prod` باشد. |
| `SCM_CONFIG_APPLICATIONS_SCM_CM_CONNECTOR` | profileهای scm-cm-connector | باید فقط `dev, test, pilot, prod` باشد. |
