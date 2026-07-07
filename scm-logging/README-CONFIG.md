# راهنمای تنظیمات scm-logging

## 1. هدف این فایل

این فایل برای تیم پشتیبانی است تا تنظیمات `scm-logging` را بشناسد. این سرویس مسئول مصرف پیامهای log و نوشتن آنها در دیتابیسهای legacy است؛ بنابراین تنظیمات دیتابیس و MQ آن حساس هستند.

## 2. قانون کلی تنظیمات

فایل `src/main/resources/application.yml` نقشه اصلی تنظیمات است و مقدارهای واقعی را از متغیرها میخواند. در `dev` متغیرها داخل `application-dev.yml` هستند و Config Server غیرفعال است. در `test`، `pilot` و `prod` فایل profile فقط اتصال به Config Server را bootstrap میکند.

فقط profileهای `dev`، `test`، `pilot` و `prod` مجاز هستند. فایل config repo باید فقط متغیر تعریف کند، نه propertyهای تو در تو.

## 3. فایلهای مهم

- `src/main/resources/application.yml`: نگاشت اصلی تنظیمات سرویس.
- `src/main/resources/application-dev.yml`: متغیرهای توسعه و غیرفعال کردن Config Server.
- `src/main/resources/application-test.yml`: اتصال test به Config Server.
- `src/main/resources/application-pilot.yml`: اتصال pilot به Config Server.
- `src/main/resources/application-prod.yml`: اتصال prod به Config Server.
- `src/main/resources/logback-spring.xml`: خروجی فایلهای Observation.

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای سرویس است و باید یکی از `dev`، `test`، `pilot` یا `prod` باشد. |
| `SCM_APPLICATION_NAME` | نام برنامه | نام Spring application است و باید `scm-logging` باشد تا Config Server فایل درست را بدهد. |
| `SCM_INSTANCE_ID` | شناسه نمونه | شناسه instance یا pod است. برای پیدا کردن رخدادهای یک نمونه در log استفاده میشود. |
| `SCM_SERVER_PORT` | پورت سرویس | پورت HTTP سرویس است. اگر اشتباه باشد health و دسترسی سرویس مختل میشود. |
| `SCM_SERVICE_VERSION` | نسخه سرویس | نسخه ثبت شده در Observation است و برای عیب یابی release استفاده میشود. |
| `SCM_LOG_LEVEL_ROOT` | سطح log اصلی | سطح log ریشه است. در prod تغییر آن باید کوتاه مدت و کنترل شده باشد. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_LOGGING_JMS_DESTINATION` | صف log | نام مقصد JMS برای پیامهای log است. اگر اشتباه باشد سرویس پیام درست را مصرف نمیکند. |
| `SCM_LOGGING_MQ_QUEUE_MANAGER` | Queue Manager | نام Queue Manager اتصال MQ است. مقدار اشتباه باعث قطع مصرف پیام میشود. |
| `SCM_LOGGING_MQ_CHANNEL` | کانال MQ | نام channel اتصال به MQ است. |
| `SCM_LOGGING_MQ_HOST` | میزبان MQ | آدرس MQ است. اگر اشتباه باشد اتصال برقرار نمیشود. |
| `SCM_LOGGING_MQ_PORT` | پورت MQ | پورت اتصال MQ است. |
| `SCM_LOGGING_MQ_USERNAME` | کاربر MQ | نام کاربری MQ است. مقدار حساس محسوب میشود. |
| `SCM_LOGGING_MQ_PASSWORD` | رمز MQ | رمز MQ است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_LOGGING_TRANSACTION_CONVERTER_ENABLED` | تبدیل transaction log | فعال بودن تبدیل transaction log است. خاموش کردن آن باعث توقف نوشتن این نوع log میشود. |
| `SCM_LOGGING_TRACE_CONVERTER_ENABLED` | تبدیل trace | فعال بودن تبدیل trace legacy است. خاموش کردن آن باعث ناقص شدن داده trace legacy میشود. |
| `SCM_LOGGING_MESSAGE_CONVERTER_ENABLED` | تبدیل message log | فعال بودن تبدیل message log است. خاموش کردن آن باید فقط با هماهنگی انجام شود. |
| `SCM_LOGGING_SCHEDULER_FIXED_RATE` | دوره scheduler | فاصله اجرای scheduler بر حسب میلی ثانیه است. مقدار خیلی کم میتواند فشار دیتابیس را زیاد کند. |
| `SCM_CACHE_ADDRESS` | آدرس cache | آدرس Hazelcast cache است. اگر اشتباه باشد قابلیتهای وابسته به cache دچار خطا میشود. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر cache | نام کلاستر Hazelcast است و باید با محیط هماهنگ باشد. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر ریشه Observation | مسیر ریشه فایلهای log، trace و audit است. اگر قابل نوشتن نباشد خروجیها تولید نمیشوند. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | log کنسول | در dev میتواند روشن باشد. در pilot/prod معمولاً خاموش است. |
| `SCM_OBS_LOG_FILE_ENABLED` | log فایل | فعال بودن فایل NDJSON لاگ است. اگر خاموش باشد Filebeat چیزی ارسال نمیکند. |
| `SCM_OBS_TRACE_FILE_ENABLED` | trace فایل | فعال بودن فایل trace است. برای عیب یابی مصرف پیامها مفید است. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit پیش فرض خاموش است. فقط با نیاز عملیاتی روشن شود. |
| `SCM_OBS_METRIC_ENABLED` | metric | metric از مسیر Actuator/Micrometer به Prometheus میرود و به فایل نوشته نمیشود. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | فقط در `test`، `pilot` و `prod` استفاده میشود. مقدار اشتباه مانع دریافت config میشود. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری Config Server است. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_FAIL_FAST` | توقف هنگام خطا | اگر Config Server در دسترس نباشد سرویس باید fail شود، مخصوصاً در pilot/prod. |
| `SCM_CONFIG_RETRY_MAX_ATTEMPTS` | تعداد retry | تعداد تلاش اتصال به Config Server است. |

## متغیرهای دیتابیسهای legacy

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_LOGGING_TRANSACTION_DB_URL` | دیتابیس transaction log | آدرس JDBC دیتابیس transaction log است. مقدار اشتباه باعث توقف ذخیره transaction log میشود. |
| `SCM_LOGGING_TRANSACTION_DB_USERNAME` | کاربر transaction DB | نام کاربری دیتابیس transaction log است. |
| `SCM_LOGGING_TRANSACTION_DB_PASSWORD` | رمز transaction DB | رمز این دیتابیس حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_LOGGING_TRACE_DB_URL` | دیتابیس trace | آدرس JDBC دیتابیس trace legacy است. مقدار اشتباه باعث خطای تبدیل trace میشود. |
| `SCM_LOGGING_TRACE_DB_USERNAME` | کاربر trace DB | نام کاربری دیتابیس trace است. |
| `SCM_LOGGING_TRACE_DB_PASSWORD` | رمز trace DB | رمز دیتابیس trace حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_LOGGING_MESSAGE_DB_URL` | دیتابیس message log | آدرس JDBC دیتابیس message log است. مقدار اشتباه باعث توقف ذخیره message log میشود. |
| `SCM_LOGGING_MESSAGE_DB_USERNAME` | کاربر message DB | نام کاربری دیتابیس message log است. |
| `SCM_LOGGING_MESSAGE_DB_PASSWORD` | رمز message DB | رمز دیتابیس message log حساس است. نباید در ticket، screenshot یا chat ارسال شود. |

## 8. مواردی که نباید دوباره استفاده شوند

- profileهای قدیمی مثل `ib4dev`، `ib4test`، `scm4dev`، `scm4test`، `mb4dev`، `mb4test`، `pwa4dev` و `pwa4test` نباید فعال شوند.
- مسیرهای قدیمی فایل log نباید با `scm.log.app.*` تنظیم شوند. خروجی برنامه از `scm.observation.*` و `SCM_OBS_ROOT_DIR` میآید.
- متغیرهای دیتابیس و MQ حساس هستند و نباید در ticket، screenshot یا chat ارسال شوند.
- فایلهای config repo نباید propertyهایی مثل `scm.datasource.transactionLog.url` را مستقیم تعریف کنند؛ فقط متغیرهایی مثل `SCM_LOGGING_TRANSACTION_DB_URL` تعریف شوند.

## 9. نکات خاص هر محیط

- در `dev` مقدارها محلی یا تستی هستند و Config Server خاموش است.
- در `test` Config Server اجباری است و retry بیشتری دارد.
- در `pilot` و `prod` از `optional:configserver:` استفاده نمیشود و سرویس باید در نبود Config Server بالا نیاید.
- در `pilot` و `prod` همه رمزهای دیتابیس و MQ باید از Secret Management یا مسیر امن سازمانی تامین شوند.
