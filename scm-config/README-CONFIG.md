# راهنمای تنظیمات scm-config

## 1. هدف این فایل

این فایل برای تیم پشتیبانی است تا تنظیمات `scm-config` را بشناسد. این سرویس خود Config Server است و نباید برای دریافت config به خودش وصل شود.

## 2. قانون کلی تنظیمات

فایل `src/main/resources/application.yml` نقشه اصلی تنظیمات Config Server است. مقدارهای واقعی باید از متغیرها تامین شوند. فایلهای `application-dev.yml`، `application-test.yml`، `application-pilot.yml` و `application-prod.yml` فقط متغیرهای محلی همین سرویس را تعریف میکنند و Config Client bootstrap ندارند.

فقط profileهای `dev`، `test`، `pilot` و `prod` در mapping برنامه ها مجاز هستند.

## 3. فایلهای مهم

- `src/main/resources/application.yml`: نگاشت اصلی Config Server به متغیرها.
- `src/main/resources/application-dev.yml`: متغیرهای توسعه.
- `src/main/resources/application-test.yml`: متغیرهای محیط test.
- `src/main/resources/application-pilot.yml`: متغیرهای محیط pilot.
- `src/main/resources/application-prod.yml`: متغیرهای محیط prod.
- `src/main/resources/logback-spring.xml`: خروجی Observation برای خود Config Server.

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای Config Server است و باید یکی از `dev`، `test`، `pilot` یا `prod` باشد. |
| `SCM_APPLICATION_NAME` | نام برنامه | نام Spring application است و باید `scm-config` باشد. |
| `SCM_INSTANCE_ID` | شناسه نمونه | شناسه pod یا instance است. برای عیب یابی logها استفاده میشود. |
| `SCM_CONFIG_SERVER_PORT` | پورت Config Server | پورت سرویس Config Server است. اگر اشتباه باشد سرویسهای دیگر config را دریافت نمیکنند. |
| `SCM_CONFIG_SERVER_PREFIX` | پیشوند مسیر | پیشوند endpointهای Config Server است. مقدار فعلی معمولاً `/config` است و تغییر آن باید با همه clientها هماهنگ شود. |
| `SCM_CONFIG_LOG_PATH` | مسیر log Spring | مسیر پایه log داخلی Spring است. خروجی Observation جداگانه از `SCM_OBS_ROOT_DIR` ساخته میشود. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_REPO_URI` | آدرس مخزن config | آدرس git یا مسیر local/bare repo است. اگر اشتباه باشد Config Server هیچ configی پیدا نمیکند. |
| `SCM_CONFIG_REPO_SEARCH_PATHS` | مسیر جستجو | مسیرهای جستجوی config repo است. مقدار رایج `{application}` است. اشتباه بودن آن باعث خالی برگشتن config میشود. |
| `SCM_CONFIG_REPO_BRANCH` | شاخه مخزن | branch پیش فرض config repo است. اگر اشتباه باشد config محیط پیدا نمیشود. |
| `SCM_CONFIG_REPO_BASEDIR` | مسیر clone محلی | مسیر clone کاری Config Server است. باید قابل نوشتن باشد. |
| `SCM_CONFIG_NATIVE_SEARCH_LOCATIONS` | مسیر native | مسیر native repository در صورت استفاده از native profile است. اگر اشتباه باشد config native پیدا نمیشود. |
| `SCM_CONFIG_ADMIN_USERNAME` | کاربر admin | کاربر مدیریتی خود Config Server است. |
| `SCM_CONFIG_ADMIN_PASSWORD` | رمز admin | رمز admin حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_USERNAME` | کاربر clientها | نام کاربری اصلی clientها برای خواندن config است. |
| `SCM_CONFIG_PASSWORD` | رمز clientها | رمز clientها حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_CFG_USERNAME` | کاربر config | کاربر نقش config است. فقط برای عملیات مجاز استفاده شود. |
| `SCM_CONFIG_CFG_PASSWORD` | رمز config | رمز کاربر config حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_OPR_USERNAME` | کاربر operator | کاربر مشاهده/عملیات محدود است. |
| `SCM_CONFIG_OPR_PASSWORD` | رمز operator | رمز operator حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_ENCRYPT_KEY` | کلید رمزنگاری | کلید encrypt/decrypt مقدارهای `{cipher}` است. بسیار حساس است و نباید در ticket، screenshot یا chat ارسال شود. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر ریشه Observation | مسیر ریشه فایلهای log، trace و audit خود Config Server است. اگر قابل نوشتن نباشد خروجیها تولید نمیشوند. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | log کنسول | در dev معمولاً روشن است و در pilot/prod معمولاً خاموش میشود. |
| `SCM_OBS_LOG_FILE_ENABLED` | log فایل | فعال بودن فایل NDJSON لاگ است. اگر خاموش باشد Filebeat لاگ Config Server را جمع نمیکند. |
| `SCM_OBS_TRACE_FILE_ENABLED` | trace فایل | فعال بودن trace است. برای بررسی درخواستهای config مفید است. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit پیش فرض خاموش است. فقط با نیاز عملیاتی روشن شود. |
| `SCM_OBS_METRIC_ENABLED` | metric | metric از مسیر Actuator/Micrometer به Prometheus میرود و به فایل نوشته نمیشود. |

## 7. متغیرهای اتصال به Config Server

برای `scm-config` کاربرد ندارد، چون این سرویس خودش Config Server است و نباید Config Client bootstrap داشته باشد.

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | برای خود `scm-config` استفاده نمیشود. این متغیر مخصوص clientها مثل `scm-cache` و `scm-uaa` است. |

## متغیرهای mapping برنامه ها

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_APPLICATIONS_SCM_WEB` | profileهای scm-web | باید فقط `dev, test, pilot, prod` باشد. مقدار دیگر باعث برگشت profileهای قدیمی میشود. |
| `SCM_CONFIG_APPLICATIONS_SCM_CACHE` | profileهای scm-cache | باید فقط `dev, test, pilot, prod` باشد. |
| `SCM_CONFIG_APPLICATIONS_SCM_UAA` | profileهای scm-uaa | باید فقط `dev, test, pilot, prod` باشد. |
| `SCM_CONFIG_APPLICATIONS_SCM_LOGGING` | profileهای scm-logging | باید فقط `dev, test, pilot, prod` باشد. |
| `SCM_CONFIG_APPLICATIONS_SCM_CM_CONNECTOR` | profileهای CM connector | باید فقط `dev, test, pilot, prod` باشد. |

## 8. مواردی که نباید دوباره استفاده شوند

- `scm-config` نباید `spring.config.import=configserver:` داشته باشد و نباید از خودش config بگیرد.
- profileهای قدیمی مثل `ib4dev`، `ib4test`، `scm4dev`، `scm4test`، `mb4dev`، `mb4test`، `pwa4dev` و `pwa4test` نباید در mapping برنامه ها برگردند.
- کلیدهای رمزنگاری و passwordها نباید hardcode شوند و نباید در ticket، screenshot یا chat ارسال شوند.
- فایلهای config repo باید فقط متغیرهایی مثل `SCM_DB_URL` داشته باشند، نه propertyهای تو در تو مثل `scm.datasource.primary.url`.

## 9. نکات خاص هر محیط

- در `dev` مقدارهای placeholder فقط برای توسعه هستند و نباید در test/pilot/prod استفاده شوند.
- در `test`، `pilot` و `prod` رمزها و `SCM_CONFIG_ENCRYPT_KEY` باید از Secret Management یا مسیر امن سازمانی تامین شوند.
- تغییر `SCM_CONFIG_SERVER_PREFIX` یا `SCM_CONFIG_REPO_SEARCH_PATHS` روی همه سرویسهای وابسته اثر دارد.
- قبل از تغییر mapping برنامه ها مطمئن شوید profileهای مجاز فقط `dev`، `test`، `pilot` و `prod` هستند.
