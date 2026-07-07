# راهنمای تنظیمات scm-uaa

## 1. هدف این فایل

این فایل برای تیم پشتیبانی است تا بداند تنظیمات `scm-uaa` از کجا تامین میشود و اشتباه بودن هر متغیر چه اثری روی ورود کاربران، token، OTP، دیتابیس و Observation دارد.

## 2. قانون کلی تنظیمات

فایل `src/main/resources/application.yml` نقشه اصلی تنظیمات است. مقدارهای واقعی محیطی باید از متغیرها بیایند. در `dev` متغیرها داخل `application-dev.yml` هستند و Config Server غیرفعال است. در `test`، `pilot` و `prod` فایل profile فقط اتصال به Config Server را انجام میدهد.

فقط profileهای `dev`، `test`، `pilot` و `prod` مجاز هستند. تفاوت محیط یا کانال نباید با profileهای قدیمی ساخته شود.

## 3. فایلهای مهم

- `src/main/resources/application.yml`: نگاشت اصلی Spring و SCM propertyها به متغیرها.
- `src/main/resources/application-dev.yml`: متغیرهای توسعه و غیرفعال کردن Config Server.
- `src/main/resources/application-test.yml`: bootstrap اتصال test به Config Server.
- `src/main/resources/application-pilot.yml`: bootstrap اتصال pilot به Config Server.
- `src/main/resources/application-prod.yml`: bootstrap اتصال prod به Config Server.
- `src/main/resources/logback-spring.xml`: خروجی log، trace و audit بر اساس Observation.

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط سرویس است و باید یکی از `dev`، `test`، `pilot` یا `prod` باشد. مقدار اشتباه باعث ابهام در log و کنترلهای محیطی میشود. |
| `SCM_APPLICATION_NAME` | نام برنامه | نام Spring application است و باید `scm-uaa` باشد. این نام برای Config Server و فایلهای Observation استفاده میشود. |
| `SCM_INSTANCE_ID` | شناسه نمونه | شناسه pod یا instance است. اگر تکراری باشد دنبال کردن خطاها سخت میشود. |
| `SCM_SERVER_PORT` | پورت سرویس | پورت HTTP سرویس UAA است. اگر اشتباه باشد سرویس از مسیر مورد انتظار در دسترس نیست. |
| `SCM_SERVICE_VERSION` | نسخه سرویس | نسخه ثبت شده در Observation است و برای عیب یابی release استفاده میشود. |
| `SCM_LOG_LEVEL_ROOT` | سطح log اصلی | سطح log ریشه است. افزایش سطح در pilot/prod باید موقت و کنترل شده باشد. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_UAA_BASE_URL` | آدرس پایه UAA | آدرس پایه سرویس احراز هویت است. اگر اشتباه باشد سرویسهای دیگر token و JWK را درست پیدا نمیکنند. |
| `SCM_UAA_ISSUER_URL` | آدرس issuer | آدرس issuer داخل token است. تغییر اشتباه باعث نامعتبر شدن tokenها برای سرویسهای مصرف کننده میشود. |
| `SCM_UAA_JWK_SET_URL` | آدرس JWK | آدرس کلید عمومی tokenها است. اگر اشتباه باشد اعتبارسنجی token در سرویسها شکست میخورد. |
| `SCM_UAA_JWT_KEY_STORE` | فایل keystore | مسیر keystore امضای token است. اگر اشتباه باشد UAA نمیتواند token امضا کند. |
| `SCM_UAA_JWT_KEY_STORE_PASSWORD` | رمز keystore | رمز keystore است و مقدار حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_UAA_JWT_KEY_ALIAS` | alias کلید | نام کلید داخل keystore است. مقدار اشتباه باعث خطای تولید کلید امضا میشود. |
| `SCM_DB_URL` | آدرس دیتابیس اصلی | آدرس JDBC دیتابیس اصلی احراز هویت است. اگر اشتباه باشد سرویس بالا نمیآید یا کاربر و client خوانده نمیشود. |
| `SCM_DB_USERNAME` | کاربر دیتابیس اصلی | نام کاربری دیتابیس اصلی است. |
| `SCM_DB_PASSWORD` | رمز دیتابیس اصلی | رمز دیتابیس اصلی است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_DB_SCHEMA` | schema دیتابیس اصلی | schema پیش فرض دیتابیس اصلی است. مقدار اشتباه باعث خطای query میشود. |
| `SCM_UAA_ACTIVATION_DB_URL` | آدرس دیتابیس activation | آدرس دیتابیس مربوط به activation است. اگر اشتباه باشد فعالسازی کاربر یا دستگاه دچار خطا میشود. |
| `SCM_UAA_ACTIVATION_DB_PASSWORD` | رمز دیتابیس activation | رمز دیتابیس activation است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CACHE_ADDRESS` | آدرس cache | آدرس Hazelcast cache است. اگر اشتباه باشد session و JTI cache درست کار نمیکند. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر cache | نام کلاستر Hazelcast است. باید با `scm-cache` همان محیط هماهنگ باشد. |
| `SCM_OTP_TTL` | مهلت OTP | مدت اعتبار OTP بر حسب دقیقه است. مقدار خیلی کم باعث نارضایتی کاربر و مقدار خیلی زیاد ریسک امنیتی ایجاد میکند. |
| `SCM_OTP_MAX_ATTEMPTS` | تلاش OTP | تعداد تلاش مجاز برای OTP است. مقدار اشتباه روی امنیت و تجربه ورود اثر دارد. |
| `SCM_OTP_AVACAS_HOST` | میزبان MQ برای OTP | آدرس MQ مربوط به OTP است. اگر اشتباه باشد ارسال یا تایید OTP مختل میشود. |
| `SCM_OTP_AVACAS_PASSWORD` | رمز MQ برای OTP | رمز MQ مربوط به OTP است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_SECURITY_CORS_ALLOWED_ORIGINS` | originهای مجاز | فهرست originهای مجاز برای مرورگر است. در `test`، `pilot` و `prod` نباید خالی باشد و مقدار اشتباه باعث خطای CORS میشود. |
| `SCM_LOGOUT_JMS_ENABLED` | فعال بودن logout MQ | اگر فعال باشد پیام logout به MQ ارسال میشود. مقدار اشتباه میتواند logout سراسری را مختل کند. |
| `SCM_LOGOUT_JMS_PASSWORD` | رمز MQ خروج | رمز اتصال MQ برای logout است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_NOTIFICATION_ENABLED` | فعال بودن notification | اگر فعال باشد ارسال پیامک یا notification فعال میشود. مقدار اشتباه میتواند پیامهای عملیاتی را قطع کند. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر ریشه Observation | مسیر ریشه فایلهای log، trace و audit است. اگر قابل نوشتن نباشد خروجیها تولید نمیشوند. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | log کنسول | در dev معمولاً روشن است. در pilot/prod معمولاً خاموش نگه داشته میشود تا خروجی کنسول کنترل شود. |
| `SCM_OBS_LOG_FILE_ENABLED` | log فایل | فایل NDJSON لاگ را فعال میکند. اگر خاموش باشد Filebeat لاگ برنامه را جمع نمیکند. |
| `SCM_OBS_TRACE_FILE_ENABLED` | trace فایل | فایل trace را فعال میکند. خاموش بودن آن بررسی مسیر احراز هویت را دشوار میکند. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit رخدادهای حساس را فعال میکند. چون ممکن است داده عملیاتی مهم تولید کند باید با هماهنگی روشن شود. |
| `SCM_UAA_AUTH_TRACE_ASPECT_ENABLED` | trace احراز هویت | trace داخلی فرآیند authentication را فعال میکند. برای عیب یابی مفید است ولی حجم trace را زیاد میکند. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | فقط در `test`، `pilot` و `prod` استفاده میشود. اگر اشتباه باشد UAA config محیط را دریافت نمیکند. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری اتصال به Config Server است. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_FAIL_FAST` | توقف هنگام خطا | برای pilot/prod باید `true` بماند تا سرویس با config ناقص بالا نیاید. |
| `SCM_CONFIG_RETRY_MAX_ATTEMPTS` | تعداد retry | تعداد تلاش برای اتصال به Config Server است. در test معمولاً بیشتر از pilot/prod است. |

## 8. مواردی که نباید دوباره استفاده شوند

- profileهای قدیمی مثل `ib4dev`، `ib4test`، `scm4dev`، `scm4test`، `mb4dev`، `mb4test`، `pwa4dev` و `pwa4test` نباید فعال شوند.
- مسیرهای قدیمی `scm.log.app.*` نباید برای فایل لاگ استفاده شوند. مسیرهای log، trace و audit از `SCM_OBS_ROOT_DIR` ساخته میشوند.
- مقدارهای حساس مثل رمز دیتابیس، رمز MQ، keystore password و Config Server password نباید در ticket، screenshot یا chat ارسال شوند.
- فایلهای config repo نباید propertyهای تو در تو مثل `scm.uaa.datasource.main.url` را تکرار کنند؛ فقط متغیرهایی مثل `SCM_DB_URL` تعریف شوند.

## 9. نکات خاص هر محیط

- در `dev` Config Server خاموش است و مقدارها برای توسعه محلی تعریف شده اند.
- در `test` Config Server اجباری است و retry بیشتری دارد.
- در `pilot` و `prod` از `optional:configserver:` استفاده نمیشود و سرویس باید در نبود Config Server fail شود.
- در `pilot` و `prod` مقدارهای `SCM_DB_PASSWORD`، `SCM_UAA_JWT_KEY_STORE_PASSWORD`، `SCM_OTP_AVACAS_PASSWORD` و `SCM_CONFIG_PASSWORD` باید از مسیر امن سازمانی تامین شوند.
