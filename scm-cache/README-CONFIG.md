# راهنمای تنظیمات scm-cache

## 1. هدف این فایل

| کلید | عنوان | توضیحات |
|---|---|---|
| `scm-cache` | هدف راهنما | این فایل برای پشتیبانی توضیح میدهد تنظیمات cache server از کجا میآیند و خطای مقداردهی چه اثری دارد. |
| `application.yml` | نقشه اصلی | propertyهای واقعی Spring، datasource و Hazelcast در این فایل تعریف میشوند و مقدار آنها از متغیرها خوانده میشود. |

## 2. قانون کلی تنظیمات

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev,test,pilot,prod` | profileهای مجاز | فقط این چهار profile مجاز هستند و profileهای قدیمی نباید فعال شوند. |
| `SCM_*` | منبع مقداردهی | profileها و Config Server باید فقط متغیر flat تعریف کنند و propertyهای nested Hazelcast یا Spring را تکرار نکنند. |
| `SCM_CACHE_CLUSTER_NAME` | نام محیطی کلاستر | نام کلاستر باید معنی دار و وابسته به محیط باشد، مثل `scm-cache-prod`. |
| `SCM_CACHE_MEMBER_NAME` | نام عضو | نام عضو باید node یا pod را مشخص کند. مقدار تکراری عیب یابی cluster را سخت میکند. |

## 3. فایلهای مهم

| کلید | عنوان | توضیحات |
|---|---|---|
| `src/main/resources/application.yml` | فایل اصلی تنظیمات | mapper مرکزی سرویس است و real propertyها را از متغیرها میخواند. |
| `src/main/resources/application-dev.yml` | تنظیمات dev | Config Server را خاموش میکند و مقدارهای local/dev را تعریف میکند. |
| `src/main/resources/application-test.yml` | تنظیمات test | فقط Config Server را bootstrap میکند و retry بیشتری دارد. |
| `src/main/resources/application-pilot.yml` | تنظیمات pilot | فقط Config Server را bootstrap میکند و نبود Config Server باید startup را شکست دهد. |
| `src/main/resources/application-prod.yml` | تنظیمات prod | فقط Config Server را bootstrap میکند و مقدارهای حساس باید از Secret یا محیط امن بیایند. |

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای سرویس است. مقدار اشتباه باعث label اشتباه در log و trace میشود. |
| `SCM_APP` | نام برنامه | نام Spring application است و معمولاً باید `scm-cache` باشد. |
| `SCM_METADATA_NAMESPACE` | namespace اجرا | در Kubernetes از metadata.namespace میآید و در dev مقدار local دارد. |
| `SCM_METADATA_INSTANCE_ID` | شناسه نمونه | شناسه node یا pod است. اگر تکراری باشد تشخیص عضو مشکل دار سخت میشود. |
| `SCM_METADATA_TIME_ZONE` | timezone فایل | اگر خالی باشد timezone سیستم JVM استفاده میشود و فقط برای نام فایلهای observation است. |
| `SCM_SERVER_PORT` | پورت مدیریت | پورت HTTP مربوط به Actuator، health و probeها است. مقدار اشتباه باعث شکست health check و probeها میشود. |
| `SCM_DB_URL` | آدرس دیتابیس | آدرس JDBC دیتابیس مورد نیاز cache است. اگر اشتباه باشد سرویس بالا نمیآید. |
| `SCM_DB_USERNAME` | کاربر دیتابیس | نام کاربری دیتابیس است و مقدار حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_DB_PASSWORD` | رمز دیتابیس | رمز دیتابیس است و کاملاً حساس است. در pilot/prod باید از Secret Management بیاید. |
| `SCM_DB_SCHEMA` | schema دیتابیس | schema پیشفرض دیتابیس است. مقدار اشتباه باعث خطای جدول یا query میشود. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر | نام کلاستر Hazelcast است. مقدار درست باید برای محیطهای dev، test، pilot و prod جدا باشد. |
| `SCM_CACHE_MEMBER_NAME` | نام عضو | نام عضو Hazelcast است. در Kubernetes بهتر است از hostname pod استفاده شود. |
| `SCM_CACHE_NETWORK_PORT` | پورت Hazelcast | پورت network Hazelcast است. مقدار اشتباه باعث join نشدن nodeها میشود. |
| `SCM_CACHE_NETWORK_PORT_AUTO_INCREMENT` | افزایش خودکار پورت | اگر true باشد Hazelcast در صورت اشغال بودن پورت، پورت بعدی را امتحان میکند. |
| `SCM_CACHE_TCP_IP_ENABLED` | کشف TCP/IP | روشن یا خاموش بودن discovery دستی TCP/IP را مشخص میکند. |
| `SCM_CACHE_TCP_IP_MEMBERS` | اعضای TCP/IP | فهرست اعضای cache برای discovery دستی است. مقدار اشتباه باعث تشکیل نشدن cluster میشود. |
| `SCM_CACHE_KUBERNETES_ENABLED` | کشف Kubernetes | در محیطهای cluster معمولاً باید فعال باشد تا اعضا از service پیدا شوند. |
| `SCM_CACHE_KUBERNETES_NAMESPACE` | namespace کش | namespace Kubernetes برای discovery است. مقدار اشتباه باعث پیدا نشدن podها میشود. |
| `SCM_CACHE_KUBERNETES_SERVICE_NAME` | service کش | نام Kubernetes service مربوط به cache است. مقدار اشتباه باعث join نشدن cluster میشود. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر اصلی observation | مسیر پایه log، trace و audit است. مقدار اشتباه باعث نوشته نشدن فایلها در مسیر مورد انتظار میشود. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | لاگ کنسول | فقط در profile `dev` باید روشن باشد. در test، pilot و prod خاموش است. |
| `SCM_OBS_LOG_FILE_ENABLED` | لاگ فایل | فایل log را روشن یا خاموش میکند. خاموش بودن اشتباه باعث از دست رفتن لاگ عملیاتی میشود. |
| `SCM_OBS_TRACE_FILE_ENABLED` | فایل trace | مقصد فایل TRACEهای داخلی cache را کنترل میکند و HTTP مدیریت را trace نمیکند. |
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
| `ib4dev,ib4test,scm4dev,scm4test,mb4dev,mb4test,pwa4dev,pwa4test` | profileهای قدیمی | این profileها نباید برای cache server فعال شوند. |
| `نام مبهم قدیمی کلاستر` | نام غیرمجاز | نامهای مبهم برای کلاستر cache مجاز نیستند و باید با نامهایی مثل `scm-cache-dev` جایگزین شوند. |
| `نام مبهم قدیمی عضو` | نام غیرمجاز | نامهای مبهم برای عضو cache مجاز نیستند و باید با نام node یا pod جایگزین شوند. |
| `scm.log.app.*` | تنظیم قدیمی logback | مسیرهای log باید از observation و `SCM_OBS_ROOT_DIR` ساخته شوند. |

## 9. نکات خاص هر محیط

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev` | محیط توسعه | Config Server خاموش است و کلاستر با `SCM_CACHE_CLUSTER_NAME=scm-cache-dev` و `SCM_CACHE_MEMBER_NAME=scm-cache-dev-local` اجرا میشود. |
| `test` | محیط تست | مقدار کلاستر باید `scm-cache-test` باشد و عضو معمولاً از hostname گرفته میشود. |
| `pilot` | محیط پایلوت | مقدار کلاستر باید `scm-cache-pilot` باشد و credentialهای دیتابیس نباید hardcode شوند. |
| `prod` | محیط عملیاتی | مقدار کلاستر باید `scm-cache-prod` باشد و نبود Config Server یا Secret باید مانع startup شود. |
