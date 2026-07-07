# راهنمای تنظیمات scm-cache

## 1. هدف این فایل

این فایل برای تیم پشتیبانی است تا بداند تنظیمات `scm-cache` از کجا خوانده میشود و هر متغیر چه اثری روی بالا آمدن سرویس، Hazelcast، دیتابیس تنظیمات cache و خروجیهای Observation دارد.

## 2. قانون کلی تنظیمات

فایل `src/main/resources/application.yml` نقشه اصلی تنظیمات است و propertyهای واقعی Spring و SCM را از متغیرها میخواند. در `dev` مقدار متغیرها داخل `application-dev.yml` است و Config Server غیرفعال است. در `test`، `pilot` و `prod` فایل profile فقط اتصال به Config Server را bootstrap میکند.

فقط profileهای `dev`، `test`، `pilot` و `prod` مجاز هستند. profileهای قدیمی مثل `ib4test` یا `scm4test` نباید برای این سرویس استفاده شوند.

## 3. فایلهای مهم

- `src/main/resources/application.yml`: نقشه اصلی تنظیمات و نگاشت متغیرها.
- `src/main/resources/application-dev.yml`: متغیرهای محیط توسعه و غیرفعال کردن Config Server.
- `src/main/resources/application-test.yml`: اتصال محیط test به Config Server.
- `src/main/resources/application-pilot.yml`: اتصال محیط pilot به Config Server.
- `src/main/resources/application-prod.yml`: اتصال محیط prod به Config Server.
- `src/main/resources/logback-spring.xml`: اتصال log، trace و audit به تنظیمات Observation.

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای سرویس است. مقدار عملیاتی باید یکی از `dev`، `test`، `pilot` یا `prod` باشد. اگر اشتباه باشد، تشخیص محیط در log و عیب یابی سخت میشود. |
| `SCM_APPLICATION_NAME` | نام برنامه | نام Spring application است و باید `scm-cache` بماند مگر با هماهنگی تغییر کند. این نام برای Config Server و فایلهای Observation مهم است. |
| `SCM_INSTANCE_ID` | شناسه نمونه | شناسه instance یا pod است. اگر تکراری باشد، دنبال کردن رخدادهای یک نمونه در Kibana دشوار میشود. |
| `SCM_SERVER_PORT` | پورت HTTP | پورت API و health سرویس است. اگر اشتباه باشد readiness/liveness یا دسترسی پشتیبانی به سرویس دچار مشکل میشود. |
| `SCM_SERVICE_VERSION` | نسخه سرویس | نسخه ثبت شده در Observation است. برای تطبیق رخدادها با release استفاده میشود. |
| `SCM_MANAGEMENT_ENDPOINTS_INCLUDE` | endpointهای Actuator | endpointهای Actuator قابل نمایش را مشخص میکند. metric از مسیر استاندارد Actuator/Micrometer میرود و نباید به فایل نوشته شود. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_DB_URL` | آدرس دیتابیس تنظیمات cache | آدرس JDBC دیتابیسی است که تعریف cacheها از آن خوانده میشود. اگر اشتباه باشد سرویس بالا نمیآید یا cacheها درست ساخته نمیشوند. |
| `SCM_DB_USERNAME` | کاربر دیتابیس | نام کاربری اتصال به دیتابیس تنظیمات cache است. |
| `SCM_DB_PASSWORD` | رمز دیتابیس | رمز دیتابیس است و مقدار حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_DB_SCHEMA` | schema دیتابیس | schema جدولهای تنظیمات cache است. اگر اشتباه باشد سرویس جدول تنظیمات را پیدا نمیکند. |
| `SCM_DB_MAX_CONNECTION` | تعداد connection | بیشینه connectionهای دیتابیس است. افزایش آن باید با ظرفیت دیتابیس هماهنگ باشد. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر | نام کلاستر Hazelcast است. همه memberهای یک کلاستر باید مقدار یکسان داشته باشند. |
| `SCM_CACHE_MEMBER_NAME` | نام member | نام member همین instance است. برای شناسایی node در log و health استفاده میشود. |
| `SCM_CACHE_NETWORK_PORT` | پورت Hazelcast | پورت شبکه Hazelcast است. اگر با Service یا firewall هماهنگ نباشد memberها همدیگر را پیدا نمیکنند. |
| `SCM_CACHE_TCP_IP_ENABLED` | کشف با TCP | اگر `true` باشد Hazelcast از آدرسهای TCP استفاده میکند. مقدار اشتباه باعث جدا شدن node از کلاستر میشود. |
| `SCM_CACHE_TCP_IP_MEMBERS` | memberهای TCP | فهرست memberهای Hazelcast در حالت TCP است. در Kubernetes معمولاً به جای آن discovery سرویس استفاده میشود. |
| `SCM_CACHE_KUBERNETES_ENABLED` | کشف Kubernetes | اگر `true` باشد Hazelcast از Kubernetes discovery استفاده میکند. برای pilot/prod معمولاً باید با namespace و service درست تنظیم شود. |
| `SCM_CACHE_KUBERNETES_NAMESPACE` | namespace کش | namespace Kubernetes برای کشف memberها است. مقدار اشتباه باعث تشکیل کلاستر جدا میشود. |
| `SCM_CACHE_KUBERNETES_SERVICE_NAME` | نام سرویس Kubernetes | نام Service مربوط به Hazelcast است. اگر اشتباه باشد memberها پیدا نمیشوند. |
| `SCM_CACHE_MANAGEMENT_ENABLED` | Management Center | فعال بودن اتصال Hazelcast Management Center است. فقط در صورت نیاز عملیاتی فعال شود. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر ریشه Observation | مسیر ریشه فایلهای log، trace و audit است. اگر مسیر قابل نوشتن نباشد فایلها ایجاد نمیشوند. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | log کنسول | فعال بودن log کنسول است. در dev معمولاً روشن است و در pilot/prod معمولاً خاموش میشود. |
| `SCM_OBS_LOG_FILE_ENABLED` | log فایل | فعال بودن فایل NDJSON لاگ است. اگر خاموش شود Filebeat چیزی برای ارسال به Elasticsearch ندارد. |
| `SCM_OBS_TRACE_FILE_ENABLED` | trace فایل | فعال بودن فایل NDJSON trace است. اگر خاموش باشد ردیابی درخواستها ناقص میشود. |
| `SCM_OBS_AUDIT_ENABLED` | audit | فعال بودن audit است. فقط وقتی نیاز عملیاتی وجود دارد روشن شود تا حجم و حساسیت داده کنترل شود. |
| `SCM_OBS_METRIC_ENABLED` | metric | فعال بودن metric داخلی است. metric همچنان از Actuator/Micrometer به Prometheus میرود و به فایل نوشته نمیشود. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | فقط در `test`، `pilot` و `prod` استفاده میشود. اگر اشتباه باشد سرویس config محیط را دریافت نمیکند و بالا نمیآید. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری اتصال به Config Server است. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_FAIL_FAST` | توقف هنگام خطا | اگر `true` باشد، در نبود Config Server سرویس fail میکند. برای pilot/prod باید همین رفتار حفظ شود. |
| `SCM_CONFIG_RETRY_MAX_ATTEMPTS` | تعداد retry | تعداد تلاش برای اتصال به Config Server است. در test میتواند بیشتر از pilot/prod باشد. |
| `SCM_CONFIG_RETRY_MAX_INTERVAL` | فاصله retry | بیشینه فاصله retry بر حسب میلی ثانیه است. |

## 8. مواردی که نباید دوباره استفاده شوند

- profileهای قدیمی مثل `ib4dev`، `ib4test`، `scm4dev`، `scm4test`، `mb4dev`، `mb4test`، `pwa4dev` و `pwa4test` نباید فعال شوند.
- تنظیمات واقعی مثل `scm.cache.hazelcast.member.cluster-name` نباید در فایلهای profile یا config repo تکرار شود؛ فقط متغیرهایی مثل `SCM_CACHE_CLUSTER_NAME` تعریف شوند.
- مسیرهای Observation نباید جداگانه و ناسازگار تعریف شوند؛ ریشه باید از `SCM_OBS_ROOT_DIR` بیاید.
- رمز دیتابیس و رمز Config Server نباید در پیامها، screenshotها یا ticketها نوشته شود.

## 9. نکات خاص هر محیط

- در `dev` Config Server خاموش است و مقدارها از `application-dev.yml` خوانده میشود.
- در `test` سرویس از Config Server میخواند و retry بیشتری دارد.
- در `pilot` و `prod` از `optional:configserver:` استفاده نمیشود؛ اگر Config Server در دسترس نباشد سرویس نباید با config ناقص بالا بیاید.
- در Kubernetes مقدارهای discovery باید با namespace و Service واقعی همان محیط هماهنگ باشند.
