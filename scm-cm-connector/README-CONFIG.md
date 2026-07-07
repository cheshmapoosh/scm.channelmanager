# راهنمای تنظیمات scm-cm-connector

## 1. هدف این فایل

این فایل برای تیم پشتیبانی است تا تنظیمات `scm-cm-connector` را بشناسد. این سرویس در وضعیت فعلی بیشتر به UAA، cache، امنیت resource server و Observation وابسته است.

## 2. قانون کلی تنظیمات

فایل `src/main/resources/application.yml` نقشه اصلی تنظیمات است و مقدارها از متغیرها خوانده میشوند. در `dev` مقدارها داخل `application-dev.yml` هستند و Config Server غیرفعال است. در `test`، `pilot` و `prod` فایل profile فقط اتصال به Config Server را bootstrap میکند.

فقط profileهای `dev`، `test`، `pilot` و `prod` مجاز هستند. profileهای قدیمی یا service-specific نباید برای این سرویس فعال شوند.

## 3. فایلهای مهم

- `src/main/resources/application.yml`: نگاشت اصلی تنظیمات.
- `src/main/resources/application-dev.yml`: متغیرهای توسعه و غیرفعال کردن Config Server.
- `src/main/resources/application-test.yml`: اتصال test به Config Server.
- `src/main/resources/application-pilot.yml`: اتصال pilot به Config Server.
- `src/main/resources/application-prod.yml`: اتصال prod به Config Server.
- `src/main/resources/logback-spring.xml`: اتصال log، trace و audit به Observation.

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرا است و باید یکی از `dev`، `test`، `pilot` یا `prod` باشد. |
| `SCM_APPLICATION_NAME` | نام برنامه | نام Spring application است و باید `scm-cm-connector` باشد تا Config Server فایل درست را بدهد. |
| `SCM_INSTANCE_ID` | شناسه نمونه | شناسه pod یا instance است. برای پیدا کردن رخدادهای یک نمونه استفاده میشود. |
| `SCM_SERVER_PORT` | پورت سرویس | پورت HTTP سرویس است. مقدار اشتباه باعث خطای health یا دسترسی API میشود. |
| `SCM_SERVICE_VERSION` | نسخه سرویس | نسخه ثبت شده در Observation است و برای عیب یابی release استفاده میشود. |
| `SCM_LOG_LEVEL_ROOT` | سطح log اصلی | سطح log ریشه است. در pilot/prod تغییر آن باید موقت باشد. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_UAA_BASE_URL` | آدرس پایه UAA | آدرس UAA برای اعتبارسنجی token و ساخت JWK URL است. اگر اشتباه باشد درخواستهای دارای token رد میشوند. |
| `SCM_UAA_ISSUER_URL` | آدرس issuer | issuer مورد انتظار token است. اگر با tokenهای UAA هماهنگ نباشد سرویس درخواستها را نامعتبر میکند. |
| `SCM_UAA_JWK_SET_URL` | آدرس JWK | آدرس کلید عمومی UAA است. مقدار اشتباه باعث شکست اعتبارسنجی JWT میشود. |
| `SCM_CM_CONNECTOR_RESOURCE_AUDIENCE` | audience سرویس | audience مورد انتظار برای token است. اگر با clientهای UAA هماهنگ نباشد دسترسیها رد میشوند. |
| `SCM_SECURITY_SESSION_CACHE_ENABLED` | cache session | فعال بودن cache session امنیتی است. اگر خاموش باشد خواندن session ممکن است کند یا ناموفق شود. |
| `SCM_SECURITY_SESSION_CACHE_NAME` | نام cache session | نام cache مربوط به session است. باید با تعریف cache در `scm-cache` هماهنگ باشد. |
| `SCM_CACHE_DISTRIBUTED` | cache توزیع شده | اگر `true` باشد از Hazelcast استفاده میشود. مقدار اشتباه روی session و cache اثر میگذارد. |
| `SCM_CACHE_ADDRESS` | آدرس cache | آدرس Hazelcast است. اگر اشتباه باشد سرویس به cache وصل نمیشود. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر cache | نام کلاستر Hazelcast است. باید با `scm-cache` همان محیط یکی باشد. |
| `SCM_CM_CONNECTOR_ADMIN_CACHE_ENDPOINTS_ENABLED` | endpointهای مدیریتی cache | فعال بودن endpointهای مدیریتی cache است. در pilot/prod فقط با هماهنگی فعال شود. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر ریشه Observation | مسیر ریشه فایلهای log، trace و audit است. اگر قابل نوشتن نباشد فایلها تولید نمیشوند. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | log کنسول | در dev معمولاً روشن است و در pilot/prod معمولاً خاموش میشود. |
| `SCM_OBS_LOG_FILE_ENABLED` | log فایل | فعال بودن فایل NDJSON لاگ است. اگر خاموش باشد Filebeat لاگ برنامه را جمع نمیکند. |
| `SCM_OBS_TRACE_FILE_ENABLED` | trace فایل | فعال بودن فایل trace است. برای ردیابی درخواستهای session و OTP استفاده میشود. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit پیش فرض خاموش است. فعال کردن آن باید با نیاز عملیاتی مشخص انجام شود. |
| `SCM_RESOURCE_SERVER_OBS_TRACE_ENABLED` | trace امنیت | trace مربوط به رخدادهای resource server است. اگر خاموش باشد ردیابی رد شدن token سختتر میشود. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | فقط در `test`، `pilot` و `prod` استفاده میشود. اگر اشتباه باشد سرویس config محیط را دریافت نمیکند. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری اتصال به Config Server است. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_FAIL_FAST` | توقف هنگام خطا | برای pilot/prod باید `true` بماند تا سرویس با config ناقص بالا نیاید. |
| `SCM_CONFIG_RETRY_MAX_ATTEMPTS` | تعداد retry | تعداد تلاش اتصال به Config Server است. در test معمولاً بیشتر از pilot/prod است. |

## 8. مواردی که نباید دوباره استفاده شوند

- profileهای قدیمی مثل `ib4dev`، `ib4test`، `scm4dev`، `scm4test`، `mb4dev`، `mb4test`، `pwa4dev` و `pwa4test` نباید فعال شوند.
- تنظیمات واقعی مثل `scm.security.resource-server.issuer-uri` نباید در فایلهای profile یا config repo تکرار شود؛ فقط متغیرهایی مثل `SCM_UAA_ISSUER_URL` تعریف شوند.
- مسیرهای log قدیمی یا جداگانه نباید استفاده شوند؛ مسیرها از `SCM_OBS_ROOT_DIR` ساخته میشوند.
- رمز Config Server یا هر token نباید در ticket، screenshot یا chat ارسال شود.

## 9. نکات خاص هر محیط

- در `dev` Config Server خاموش است و مقدارها برای اجرای محلی تعریف شده اند.
- در `test` Config Server اجباری است و retry بیشتری دارد.
- در `pilot` و `prod` از `optional:configserver:` استفاده نمیشود و سرویس باید در نبود Config Server fail شود.
- اگر در آینده تنظیمات endpoint، timeout، retry یا queue برای اتصال legacy CM به این سرویس اضافه شد، باید فقط در `application.yml` به متغیر نگاشت شود و فایلهای profile فقط متغیر داشته باشند.
