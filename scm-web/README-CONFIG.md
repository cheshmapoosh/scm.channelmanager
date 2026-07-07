# راهنمای تنظیمات scm-web

## 1. هدف این فایل

این فایل برای تیم پشتیبانی نوشته شده است تا بدانند تنظیمات `scm-web` از کجا خوانده میشود، هر متغیر چه کاری انجام میدهد، و در هر محیط باید چه نکاتی رعایت شود.

## 2. قانون کلی تنظیمات

فایل `application.yml` نقشه اصلی تنظیمات برنامه است. مقدارهای واقعی محیطی نباید مستقیم داخل آن نوشته شوند و باید از متغیرها تامین شوند.

در محیط `dev` متغیرها داخل `application-dev.yml` تعریف شده اند و Config Server غیرفعال است. در محیطهای `test`، `pilot` و `prod` برنامه فقط به Config Server وصل میشود و متغیرهای همان محیط باید از Config Server یا Secret Management بیایند.

تفاوت IB، SCM، MB، PWA یا هر نمونه دیگر نباید با profile جداگانه ساخته شود. فقط profileهای `dev`، `test`، `pilot` و `prod` مجاز هستند و تفاوت کانال یا نمونه باید با متغیرهایی مثل `SCM_CHANNEL_CODE` و `SCM_GATEWAY_NAME` مشخص شود.

## 3. فایلهای مهم

- `src/main/resources/application.yml`: نقشه اصلی تنظیمات برنامه و محل نگاشت متغیرها به propertyهای واقعی.
- `src/main/resources/application-dev.yml`: متغیرهای توسعه و غیرفعال کردن Config Server.
- `src/main/resources/application-test.yml`: اتصال محیط test به Config Server.
- `src/main/resources/application-pilot.yml`: اتصال محیط pilot به Config Server.
- `src/main/resources/application-prod.yml`: اتصال محیط prod به Config Server.
- `logback-spring.xml`: اتصال خروجی log، trace و audit به تنظیمات `scm.observation`.

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | نام محیط اجرای برنامه است. مقدارهای مجاز عملیاتی همان `dev`، `test`، `pilot` و `prod` هستند. در dev معمولاً داخل فایل dev می آید و Config Server غیرفعال است. در test/pilot/prod باید از Config Server یا تنظیمات محیط بیاید. |
| `SCM_APPLICATION_NAME` | نام برنامه | نام Spring application است و معمولاً باید `scm-web` باشد. این نام برای Config Server و logها مهم است. |
| `SCM_APPLICATION_VERSION` | نسخه برنامه | نسخه نمایشی برنامه در log شروع سرویس است. اگر از pipeline مقدار `VERSION` تزریق شود، همان مقدار استفاده میشود. |
| `SCM_APPLICATION_BUILD` | شماره build | شناسه build برنامه برای عیب یابی است. در pilot/prod بهتر است از pipeline بیاید. |
| `SCM_SERVICE_VERSION` | نسخه سرویس در Observation | نسخه سرویس داخل خروجیهای Observation است. برای ردیابی رخدادها در Kibana و Grafana استفاده میشود. |
| `SCM_INSTANCE_ID` | شناسه نمونه | شناسه نمونه در حال اجرا است. در dev معمولاً `local-scm-web` است. در Kubernetes میتواند از نام pod یا host بیاید. |
| `SCM_CHANNEL_CODE` | کد کانال | کد کانال فعال مثل IB، MB یا PWA است. این مقدار جایگزین profileهای قدیمی مثل `ib4test` میشود. |
| `SCM_CHANNELS` | فهرست کانالها | مقدار `scm.channels` را تعیین میکند. اگر تنظیم نشود، از `SCM_CHANNEL_CODE` استفاده میشود. |
| `SCM_GATEWAY_NAME` | نام gateway | نام gateway فعال، مثل `channel.nib` یا `domain.card` است. این مقدار مشخص میکند instance به کدام gateway وصل است. |
| `SCM_GATEWAY_NAMES` | فهرست gatewayها | برای اجرای چند gateway در یک instance استفاده میشود. مقدارها با کاما جدا میشوند. اگر تنظیم نشود، از `SCM_GATEWAY_NAME` استفاده میشود. |
| `SCM_RUNTIME_TARGET_KIND` | نوع هدف runtime | نوع runtime را مشخص میکند. مقدار رایج `CHANNEL` است. فقط در صورت نیاز عملیاتی تغییر کند. |
| `SCM_RUNTIME_CHANNEL_AFFINITY_ENABLED` | محدودسازی کانال | اگر فعال باشد، فقط کانالهای تعریف شده در `SCM_RUNTIME_ALLOWED_CHANNEL_CODES` پذیرفته میشوند. |
| `SCM_RUNTIME_ALLOWED_CHANNEL_CODES` | کانالهای مجاز | فهرست کانالهای مجاز برای channel affinity است. مقدار `*` یعنی همه کانالها مجاز هستند. |
| `SCM_SERVER_PORT` | پورت سرویس | پورت HTTP برنامه است. در deployment ممکن است از `SERVER_PORT` هم تامین شود. |
| `SCM_SERVER_CONTEXT_PATH` | مسیر context | مسیر پایه برنامه است. معمولاً `/` است و فقط با هماهنگی تغییر کند. |
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | فقط برای `test`، `pilot` و `prod` استفاده میشود. در dev استفاده نمیشود چون Config Server غیرفعال است. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری اتصال به Config Server است. اگر تنظیم نشود، از `SPRING_CLOUD_CONFIG_USERNAME` خوانده میشود. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز اتصال به Config Server است. مقدار حساس است و نباید در ticket، screenshot یا chat ارسال شود. اگر تنظیم نشود، از `SPRING_CLOUD_CONFIG_PASSWORD` خوانده میشود. |
| `SCM_CONFIG_FAIL_FAST` | توقف هنگام خطای Config Server | اگر `true` باشد و Config Server در دسترس نباشد، سرویس بالا نمی آید. برای pilot/prod باید همین رفتار حفظ شود. |
| `SCM_CONFIG_RETRY_MAX_ATTEMPTS` | تعداد تلاش اتصال به Config Server | تعداد retry برای Config Server است. در test معمولاً میتواند بیشتر باشد. در pilot/prod مقدار پیش فرض کمتر و کنترل شده است. |
| `SCM_CONFIG_RETRY_MAX_INTERVAL` | فاصله بیشینه retry | فاصله بیشینه retry اتصال به Config Server بر حسب میلی ثانیه است. |
| `SCM_MANAGEMENT_ENDPOINTS_INCLUDE` | endpointهای Actuator | endpointهای Actuator قابل نمایش را مشخص میکند. مسیر metric استاندارد Actuator حفظ شده و metric به فایل نوشته نمیشود. |
| `SCM_MANAGEMENT_HEALTH_SHOW_DETAILS` | جزئیات health | میزان نمایش جزئیات health را تعیین میکند. در prod معمولاً نباید جزئیات حساس نمایش داده شود. |
| `SCM_MANAGEMENT_HEALTH_PROBES_ENABLED` | probeهای health | فعال بودن readiness/liveness probeهای Spring Boot را مشخص میکند. |
| `SCM_LOG_LEVEL_ROOT` | سطح log اصلی | سطح log ریشه است. تغییر آن در prod باید کنترل شده باشد تا حجم log ناگهانی زیاد نشود. |
| `SCM_LOG_LEVEL_APPLICATION` | سطح log برنامه | سطح log packageهای برنامه است. برای عیب یابی کوتاه مدت میتواند موقتاً تغییر کند. |
| `SCM_LOG_LEVEL_SPRING` | سطح log Spring | سطح log Spring است. در prod معمولاً `INFO` یا بالاتر نگه داشته شود. |
| `SCM_LOG_LEVEL_HIBERNATE` | سطح log Hibernate | سطح log عمومی Hibernate است. نمایش SQL یا bind در prod ممکن است اطلاعات حساس ایجاد کند و نباید بدون هماهنگی فعال شود. |
| `SCM_LOGBACK_CONFIG` | فایل Logback | مسیر فایل Logback است. مقدار پیش فرض `classpath:logback-spring.xml` است و به تنظیمات Observation وصل شده است. |
| `SCM_CACHE_DISTRIBUTED` | کش توزیع شده | تعیین میکند کش client به صورت distributed کار کند یا نه. مقدار محیطی باید با معماری همان محیط هماهنگ باشد. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر کش | نام کلاستر cache است. در test ممکن است به کلاستر تست اشاره کند. |
| `SCM_CACHE_ADDRESS` | آدرس کش | آدرس node اصلی cache است. در dev معمولاً مقدار تستی دارد. در test/pilot/prod باید از Config Server یا تنظیمات محیط بیاید. |
| `SCM_LOGOUT_JMS_ENABLED` | فعال بودن JMS خروج | اگر فعال باشد، پیام logout به MQ ارسال میشود. در dev معمولاً غیرفعال است. |
| `SCM_LOGOUT_JMS_HOST` | میزبان JMS خروج | آدرس MQ برای پیام logout است. در test ممکن است به سرویس تست اشاره کند. |
| `SCM_LOGOUT_JMS_PORT` | پورت JMS خروج | پورت MQ برای پیام logout است. |
| `SCM_LOGOUT_JMS_QUEUE_MANAGER` | Queue Manager خروج | نام Queue Manager برای logout است. |
| `SCM_LOGOUT_JMS_CHANNEL` | کانال JMS خروج | نام channel اتصال MQ است. |
| `SCM_LOGOUT_JMS_USERNAME` | کاربر JMS خروج | نام کاربری MQ است. مقدار حساس محسوب میشود. |
| `SCM_LOGOUT_JMS_PASSWORD` | رمز JMS خروج | رمز MQ است و نباید در ticket، screenshot یا chat ارسال شود. در pilot/prod باید از Secret Management بیاید. |
| `SCM_LOGOUT_JMS_TOPIC` | topic خروج | topic پیام logout است. |
| `SCM_TRANSFORMERS_DIR` | مسیر transformerها | مسیر transformerهای Groovy است. معمولاً `/transformers` است. |
| `SCM_CAMEL_SERVLET_CONTEXT_PATH` | مسیر servlet Camel | مسیر mapping مربوط به Camel است. معمولاً `/*` است و فقط با هماهنگی فنی تغییر کند. |
| `SCM_JPA_SHOW_SQL` | نمایش SQL | اگر `true` باشد SQL در log نمایش داده میشود. در pilot/prod نباید بدون هماهنگی فعال شود چون ممکن است داده حساس ایجاد کند. |
| `SCM_JPA_OPEN_IN_VIEW` | Open In View | تنظیم Spring JPA برای open-in-view است. مقدار معمول `false` است. |
| `SCM_PROVIDER_TASK_ENABLED` | فعال بودن task provider | فعال یا غیرفعال بودن task provider داخلی را تعیین میکند. |
| `SCM_MEMBERSHIP_DEFAULT_SERVICES` | سرویسهای عضویت پیش فرض | فهرست سرویسهای پیش فرض membership است. مقدارها با کاما جدا میشوند. |
| `SCM_ASSET_PROVIDER_NAB_SERVICE_CODE` | کد سرویس asset برای NAB | کد سرویس provider مربوط به assetهای NAB است. معمولاً فقط با تغییرات عملیاتی provider تغییر میکند. |
| `SCM_CACHE_API_DOC_CATALOG_TYPE` | نوع کش API doc | نوع کش catalog مستندات API است. مقدار معمول `local` است. |
| `SCM_CACHE_API_DOC_CATALOG_TTL` | زمان ماندگاری API doc | مدت نگهداری catalog مستندات API در کش است. |
| `SCM_CACHE_API_DOC_CATALOG_MAXIMUM_SIZE` | اندازه کش API doc | بیشینه تعداد آیتمهای کش catalog مستندات API است. |
| `SCM_CACHE_USER_TYPE` | نوع کش کاربر | نوع cache مربوط به user است. مقدار رایج `near` است. |
| `SCM_CACHE_SESSION_TYPE` | نوع کش session | نوع cache مربوط به session است. مقدار رایج `near` است. |
| `SCM_CACHE_BANK_TYPE` | نوع کش بانک | نوع cache مربوط به اطلاعات بانک است. مقدار رایج `local` است. |
| `SCM_CACHE_NEAR_MAP_IN_MEMORY_FORMAT` | قالب near cache | قالب نگهداری داده در near cache است. معمولاً `object` است. |
| `SCM_CACHE_NEAR_MAP_TTL_SECONDS` | زمان near cache | زمان ماندگاری near cache بر حسب ثانیه است. |
| `SCM_GATEWAY_REST_DEFAULT_CONTRACT_NAME` | نام قرارداد REST پیش فرض | نام قرارداد پیش فرض REST gateway است. معمولاً تغییر نمیکند. |
| `SCM_GATEWAY_REST_DEFAULT_REQUEST_DECODER` | decoder درخواست REST پیش فرض | bean decoder درخواست REST پیش فرض است. تغییر اشتباه باعث خطای پردازش درخواست میشود. |
| `SCM_GATEWAY_REST_DEFAULT_RESPONSE_ENCODER` | encoder پاسخ REST پیش فرض | bean encoder پاسخ REST پیش فرض است. |
| `SCM_GATEWAY_REST_DEFAULT_FAULT_ENCODER` | encoder خطای REST پیش فرض | bean encoder خطای REST پیش فرض است. |
| `SCM_GATEWAY_REST_V1_CONTRACT_NAME` | نام قرارداد REST v1 | نام قرارداد legacy v1 است. |
| `SCM_GATEWAY_REST_V1_REQUEST_DECODER` | decoder درخواست REST v1 | decoder درخواست legacy v1 است. |
| `SCM_GATEWAY_REST_V1_RESPONSE_ENCODER` | encoder پاسخ REST v1 | encoder پاسخ legacy v1 است. |
| `SCM_GATEWAY_REST_V1_FAULT_ENCODER` | encoder خطای REST v1 | encoder خطای legacy v1 است. |
| `SCM_GATEWAY_REST_V2_CONTRACT_NAME` | نام قرارداد REST v2 | نام قرارداد REST v2 است. |
| `SCM_GATEWAY_REST_V2_REQUEST_DECODER` | decoder درخواست REST v2 | decoder درخواست REST v2 است. |
| `SCM_GATEWAY_REST_V2_RESPONSE_ENCODER` | encoder پاسخ REST v2 | encoder پاسخ REST v2 است. |
| `SCM_GATEWAY_REST_V2_FAULT_ENCODER` | encoder خطای REST v2 | encoder خطای REST v2 است. |
| `SCM_LOG_LEVEL_HAZELCAST` | سطح log Hazelcast | سطح log Hazelcast است. در prod تغییر آن باید موقت و کنترل شده باشد. |
| `SCM_LOG_LEVEL_HIBERNATE_SQL` | سطح log SQL Hibernate | سطح log SQL است. در pilot/prod فعال کردن debug میتواند اطلاعات حساس تولید کند. |
| `SCM_LOG_LEVEL_HIBERNATE_SQL_TYPE` | سطح log typeهای SQL | سطح log typeهای SQL قدیمی Hibernate است. |
| `SCM_LOG_LEVEL_HIBERNATE_BIND` | سطح log bindهای SQL | سطح log bind parameterهای Hibernate است و در محیطهای حساس نباید debug/trace شود. |

## 5. متغیرهای دیتابیس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_DB_URL` | آدرس دیتابیس | آدرس اتصال JDBC به دیتابیس اصلی `scm-web` است. در dev معمولاً به دیتابیس توسعه یا تست وصل میشود. در test/pilot/prod باید از Config Server یا تنظیمات امن محیط بیاید. |
| `SCM_DB_USERNAME` | کاربر دیتابیس | نام کاربری دیتابیس اصلی است. |
| `SCM_DB_PASSWORD` | رمز دیتابیس | رمز دیتابیس اصلی است. این مقدار حساس است و نباید در ticket، screenshot یا chat ارسال شود. در pilot/prod باید از Secret Management یا Config Server امن بیاید. |
| `SCM_DEV_DB_PASSWORD` | رمز dev دیتابیس | فقط fallback محیط dev برای `SCM_DB_PASSWORD` است. این مقدار نباید برای test/pilot/prod استفاده شود. |
| `SCM_DB_DRIVER` | Driver دیتابیس | نام driver دیتابیس است. مقدار رایج `com.ibm.db2.jcc.DB2Driver` است. |
| `SCM_DB_SCHEMA` | schema پیش فرض | schema پیش فرض دیتابیس است. مقدار رایج `REF` است. |
| `SCM_DB_MAX_CONNECTION` | تعداد connection | بیشینه connectionهای pool دیتابیس است. افزایش آن باید با ظرفیت دیتابیس هماهنگ باشد. |

## 6. متغیرهای UAA و OTP

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_UAA_BASE_URL` | آدرس سرویس UAA | آدرس پایه سرویس احراز هویت است. این مقدار همزمان سه چیز را کنترل میکند: token issuer، آدرس JWK و base URL کلاینت OTP. اگر اشتباه باشد، ورود کاربر، اعتبارسنجی token و بررسی OTP دچار مشکل میشود. |
| `SCM_OTP_REST_CLIENT_ENABLED` | فعال بودن OTP client | فعال بودن کلاینت REST مربوط به OTP را مشخص میکند. |
| `SCM_OTP_AUTH_HEADER_NAME` | نام header احراز هویت OTP | نام header ارسالی برای احراز هویت در سرویس OTP است. مقدار معمول `authorization` است. |
| `SCM_OTP_AUTH_PREFIX` | پیشوند token OTP | پیشوند token در درخواست OTP است. مقدار معمول `Bearer ` است. |
| `SCM_OTP_VERIFY_LOGGED_IN_URL` | مسیر بررسی OTP | مسیر API بررسی OTP برای کاربر login شده است. اگر تنظیم نشود، مسیر `/api/otp/verify-by-logged-in-user` استفاده میشود. |
| `SCM_RESOURCE_SERVER_ENABLED` | فعال بودن Resource Server | فعال بودن اعتبارسنجی token در `scm-web` است. |
| `SCM_RESOURCE_SERVER_METHOD_SECURITY_ENABLED` | Method Security | فعال بودن کنترل دسترسی روی methodها است. |
| `SCM_RESOURCE_SERVER_OBSERVATION_ENABLED` | Observation امنیت | فعال بودن observation برای رخدادهای resource-server است. |
| `SCM_RESOURCE_SERVER_OBS_LOG_ENABLED` | log امنیت | فعال بودن log رخدادهای resource-server است. |
| `SCM_RESOURCE_SERVER_OBS_TRACE_ENABLED` | trace امنیت | فعال بودن trace رخدادهای resource-server است. |
| `SCM_RESOURCE_SERVER_OBS_METRIC_ENABLED` | metric امنیت | فعال بودن metric رخدادهای resource-server است. metric از مسیر Actuator/Micrometer میرود و به فایل نوشته نمیشود. |
| `SCM_SECURITY_CLIENT_ID` | client id | شناسه client امنیتی است. |
| `SCM_SECURITY_CLIENT_SECRET` | client secret | secret کلاینت است. مقدار حساس است و نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_SECURITY_DISTRIBUTED` | حالت امنیت توزیع شده | اگر `true` باشد، احراز هویت با سرویس UAA توزیع شده انجام میشود. |
| `SCM_SECURITY_PERSON_SERVICE` | منبع اطلاعات شخص | مشخص میکند سرویس person از حالت local یا remote استفاده کند. |
| `SCM_SECURITY_LEGACY_AUTH_ENABLED` | احراز هویت legacy | فعال بودن authentication providerهای legacy است. تغییر آن روی ورود کاربران اثر مستقیم دارد. |
| `SCM_SECURITY_LEGACY_CLAIM_AUTH_ENABLED` | claim legacy | فعال بودن claim authentication legacy است. معمولاً غیرفعال میماند. |
| `SCM_SECURITY_SESSION_CACHE_ENABLED` | کش session | فعال بودن کش session امنیتی است. |
| `SCM_SECURITY_SESSION_CACHE_NAME` | نام کش session | نام cache مربوط به session است. |
| `SCM_SECURITY_USER_CACHE_ENABLED` | کش کاربر | فعال بودن کش اطلاعات کاربر است. |
| `SCM_SECURITY_USER_CACHE_NAME` | نام کش کاربر | نام cache مربوط به user است. |

## 7. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ENABLED` | فعال بودن Observation | کل مشاهده پذیری برنامه را فعال یا غیرفعال میکند. |
| `SCM_OBS_ROOT_DIR` | مسیر ریشه Observation | مسیر ریشه فایلهای Observation است. پوشه های log، trace و audit زیر همین root ساخته میشوند. در dev معمولاً زیر home کاربر است. در test/pilot/prod معمولاً باید به مسیر volume یا مسیر استاندارد جمع آوری log اشاره کند. |
| `SCM_OBS_LOG_ENABLED` | فعال بودن log مشاهده پذیری | فعال بودن خروجی application log ساخت یافته را مشخص میکند. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | log در console | اگر `true` باشد log روی console هم نوشته میشود. در dev معمولاً فعال است. |
| `SCM_OBS_LOG_FILE_ENABLED` | log در فایل | اگر `true` باشد application log به فایل NDJSON نوشته میشود. |
| `SCM_OBS_LOG_DIR` | مسیر log | مسیر فایلهای log است. اگر تنظیم نشود، زیر `SCM_OBS_ROOT_DIR/log` ساخته میشود. |
| `SCM_OBS_LOG_ARCHIVE_DIR` | مسیر آرشیو log | مسیر آرشیو log است. اگر تنظیم نشود، زیر `SCM_OBS_ROOT_DIR/log/archive` ساخته میشود. |
| `SCM_OBS_TRACE_ENABLED` | فعال بودن trace | فعال بودن trace را مشخص میکند. اگر مقدار جداگانه داده نشود، از وضعیت `SCM_OBS_TRACE_FILE_ENABLED` کمک گرفته میشود. |
| `SCM_OBS_TRACE_FILE_ENABLED` | trace در فایل | اگر `true` باشد trace به فایل NDJSON نوشته میشود. |
| `SCM_OBS_TRACE_DIR` | مسیر trace | مسیر فایلهای trace است. اگر تنظیم نشود، زیر `SCM_OBS_ROOT_DIR/trace` ساخته میشود. |
| `SCM_OBS_TRACE_ARCHIVE_DIR` | مسیر آرشیو trace | مسیر آرشیو trace است. اگر تنظیم نشود، زیر `SCM_OBS_ROOT_DIR/trace/archive` ساخته میشود. |
| `SCM_OBS_AUDIT_ENABLED` | فعال بودن audit | فعال بودن audit رسمی Observation است. Audit از Application Log جدا است. |
| `SCM_OBS_AUDIT_FILE_ENABLED` | audit در فایل | اگر `true` باشد audit به فایل NDJSON جداگانه نوشته میشود. |
| `SCM_OBS_AUDIT_DIR` | مسیر audit | مسیر فایلهای audit است. اگر تنظیم نشود، زیر `SCM_OBS_ROOT_DIR/audit` ساخته میشود. |
| `SCM_OBS_AUDIT_ARCHIVE_DIR` | مسیر آرشیو audit | مسیر آرشیو audit است. اگر تنظیم نشود، زیر `SCM_OBS_ROOT_DIR/audit/archive` ساخته میشود. |
| `SCM_OBS_METRIC_ENABLED` | فعال بودن metric | metric از مسیر Actuator و Micrometer تولید میشود و به فایل نوشته نمیشود. |
| `SCM_OBS_LOG_MAX_FILE_SIZE` | اندازه فایل log | بیشینه اندازه هر فایل log قبل از rolling است. |
| `SCM_OBS_LOG_FILE_FORMAT` | قالب فایل log | قالب فایل log است. مقدار عملیاتی فعلی `jsonl` است. |
| `SCM_OBS_LOG_FILE_NAME` | نام فایل log | نام فایل application log است. |
| `SCM_OBS_LOG_CONSOLE_FORMAT` | قالب console log | قالب log کنسول است. در dev معمولاً `simple` مناسب است. |
| `SCM_OBS_LOG_MAX_HISTORY` | نگهداری log | تعداد دوره های نگهداری log در rolling است. |
| `SCM_OBS_LOG_TOTAL_SIZE_CAP` | سقف حجم log | سقف حجم کل آرشیو log است. |
| `SCM_OBS_LOG_CLEAN_HISTORY_ON_START` | پاکسازی log در شروع | اگر `true` باشد تاریخچه قدیمی هنگام شروع پاکسازی میشود. در prod با احتیاط استفاده شود. |
| `SCM_OBS_TRACE_MAX_FILE_SIZE` | اندازه فایل trace | بیشینه اندازه هر فایل trace قبل از rolling است. |
| `SCM_OBS_TRACE_FILE_FORMAT` | قالب فایل trace | قالب فایل trace است. مقدار عملیاتی فعلی `jsonl` است. |
| `SCM_OBS_TRACE_FILE_NAME` | نام فایل trace | نام فایل trace است. |
| `SCM_OBS_TRACE_MAX_HISTORY` | نگهداری trace | تعداد دوره های نگهداری trace در rolling است. |
| `SCM_OBS_TRACE_TOTAL_SIZE_CAP` | سقف حجم trace | سقف حجم کل آرشیو trace است. |
| `SCM_OBS_TRACE_CLEAN_HISTORY_ON_START` | پاکسازی trace در شروع | اگر `true` باشد تاریخچه trace هنگام شروع پاکسازی میشود. |
| `SCM_OBS_TRACE_ASYNC_ENABLED` | trace async | اگر فعال باشد نوشتن trace از appender async استفاده میکند. |
| `SCM_OBS_TRACE_ASYNC_QUEUE_SIZE` | صف async trace | اندازه صف async برای trace است. |
| `SCM_OBS_TRACE_ASYNC_DISCARDING_THRESHOLD` | آستانه حذف trace | آستانه حذف پیام در async trace است. |
| `SCM_OBS_TRACE_ASYNC_NEVER_BLOCK` | block نشدن trace | اگر `true` باشد appender trace برای صف منتظر نمیماند. |
| `SCM_OBS_TRACE_ASYNC_MAX_FLUSH_TIME` | زمان flush trace | بیشینه زمان flush appender async trace بر حسب میلی ثانیه است. |
| `SCM_OBS_AUDIT_MAX_FILE_SIZE` | اندازه فایل audit | بیشینه اندازه هر فایل audit قبل از rolling است. |
| `SCM_OBS_AUDIT_FILE_FORMAT` | قالب فایل audit | قالب فایل audit است. مقدار عملیاتی فعلی `jsonl` است. |
| `SCM_OBS_AUDIT_FILE_NAME` | نام فایل audit | نام فایل audit است. |
| `SCM_OBS_AUDIT_MAX_HISTORY` | نگهداری audit | تعداد دوره های نگهداری audit در rolling است. |
| `SCM_OBS_AUDIT_TOTAL_SIZE_CAP` | سقف حجم audit | سقف حجم کل آرشیو audit است. |
| `SCM_OBS_AUDIT_CLEAN_HISTORY_ON_START` | پاکسازی audit در شروع | اگر `true` باشد تاریخچه audit هنگام شروع پاکسازی میشود. در prod با احتیاط استفاده شود. |
| `SCM_OBS_AUDIT_ASYNC_ENABLED` | audit async | اگر فعال باشد نوشتن audit از appender async استفاده میکند. |
| `SCM_OBS_AUDIT_ASYNC_QUEUE_SIZE` | صف async audit | اندازه صف async برای audit است. |
| `SCM_OBS_AUDIT_ASYNC_DISCARDING_THRESHOLD` | آستانه حذف audit | آستانه حذف پیام در async audit است. |
| `SCM_OBS_AUDIT_ASYNC_NEVER_BLOCK` | block نشدن audit | اگر `true` باشد appender audit برای صف منتظر نمیماند. |
| `SCM_OBS_AUDIT_ASYNC_MAX_FLUSH_TIME` | زمان flush audit | بیشینه زمان flush appender async audit بر حسب میلی ثانیه است. |
| `SCM_OBS_HTTP_SERVER_SPAN_NAME` | نام span HTTP | نام span پیش فرض برای requestهای HTTP است. |

## 8. متغیرهای Timeout عمومی Providerها

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_PROVIDER_DEFAULT_CONNECT_TIMEOUT_MS` | timeout اتصال عمومی | مقدار پیش فرض connect timeout برای providerها است. اگر provider مقدار اختصاصی داشته باشد، مقدار اختصاصی اولویت دارد. |
| `SCM_PROVIDER_DEFAULT_SOCKET_TIMEOUT_MS` | timeout socket عمومی | مقدار پیش فرض socket timeout است. provider-specific value اول بررسی میشود و اگر نبود این مقدار استفاده میشود. |
| `SCM_PROVIDER_DEFAULT_SEND_TIMEOUT_MS` | timeout ارسال عمومی | مقدار پیش فرض send timeout است. provider-specific value اولویت دارد. |
| `SCM_PROVIDER_DEFAULT_RESPONSE_TIMEOUT_MS` | timeout پاسخ عمومی | مقدار پیش فرض response timeout است. اگر مقدار اختصاصی provider تنظیم نشده باشد، این مقدار استفاده میشود. |
| `SCM_PROVIDER_DEFAULT_QUEUE_CAPACITY` | ظرفیت صف عمومی | ظرفیت پیش فرض صف providerها است. اگر provider مقدار اختصاصی داشته باشد، همان مقدار استفاده میشود. |

## 9. متغیرهای HPS REST Provider

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_HPS_BASE_URL` | آدرس پایه HPS | آدرس پایه HPS است و میتواند برای providerهای مرتبط استفاده شود. در test ممکن است به سرویس تست HPS اشاره کند. |
| `SCM_HPS_REST_BASE_URL` | آدرس HPS REST | آدرس کامل سرویس REST HPS است. اگر تنظیم نشود، از `SCM_HPS_BASE_URL` به همراه مسیر پیش فرض استفاده میشود. |
| `SCM_HPS_REST_ENABLED` | فعال بودن HPS REST | فعال یا غیرفعال بودن provider را مشخص میکند. |
| `SCM_HPS_REST_SCHEME` | scheme provider | scheme داخلی provider است. معمولاً `scm-rest` است و نباید بدون تغییر فنی عوض شود. |
| `SCM_HPS_REST_CONNECT_TIMEOUT_MS` | timeout اتصال HPS REST | مقدار اختصاصی connect timeout برای HPS REST است. اگر تنظیم نشود، `SCM_PROVIDER_DEFAULT_CONNECT_TIMEOUT_MS` استفاده میشود. |
| `SCM_HPS_REST_RESPONSE_TIMEOUT_MS` | timeout پاسخ HPS REST | مقدار اختصاصی response timeout برای HPS REST است. اگر تنظیم نشود، `SCM_PROVIDER_DEFAULT_RESPONSE_TIMEOUT_MS` استفاده میشود. |
| `SCM_HPS_REST_VIRTUAL_THREADS_ENABLED` | virtual thread | فعال بودن virtual thread برای callهای HPS REST است. |
| `SCM_HPS_REST_FOLLOW_REDIRECTS` | دنبال کردن redirect | سیاست follow redirect برای HPS REST است. |
| `SCM_HPS_REST_DEFAULT_METHOD` | متد پیش فرض | متد HTTP پیش فرض HPS REST است. مقدار رایج `POST` است. |
| `SCM_HPS_REST_ACCEPT_HEADER` | header Accept | مقدار header `Accept` برای HPS REST است. |
| `SCM_HPS_REST_CONTENT_TYPE_HEADER` | header Content-Type | مقدار header `Content-Type` برای HPS REST است. |
| `SCM_HPS_REST_USERNAME` | کاربر HPS REST | نام کاربری اتصال یا احراز هویت provider است. |
| `SCM_HPS_REST_PASSWORD` | رمز HPS REST | رمز provider است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. در pilot/prod باید از Secret Management بیاید. |
| `SCM_DEV_HPS_REST_PASSWORD` | رمز dev HPS REST | فقط fallback محیط dev برای `SCM_HPS_REST_PASSWORD` است و نباید برای test/pilot/prod استفاده شود. |
| `SCM_HPS_REST_AUTH_CUSTOMIZER_TYPE` | نوع customizer احراز هویت | نوع customizer احراز هویت HPS REST است. معمولاً تغییر نمیکند. |
| `SCM_HPS_REST_AUTH_TYPE` | نوع احراز هویت | نوع احراز هویت provider است. مقدار رایج `BASIC` است. |
| `SCM_HPS_REST_AUTH_HEADER_NAME` | header احراز هویت | نام header احراز هویت provider است. |
| `SCM_HPS_REST_BASIC_BASE64` | base64 احراز هویت | مشخص میکند مقدار Basic auth به صورت base64 ساخته شود یا نه. |
| `SCM_HPS_REST_PIN_BLOCK_CUSTOMIZER_TYPE` | customizer PIN block | نوع customizer ساخت PIN block برای HPS REST است. |
| `SCM_HPS_REST_PIN_SOURCE` | منبع PIN | مسیر فیلد PIN در payload داخلی است. این مقدار به ساختار پیام وابسته است. |
| `SCM_HPS_REST_PAN_SOURCE` | منبع PAN | مسیر فیلد PAN در payload داخلی است. این مقدار میتواند حساس باشد و نباید در log خام قرار گیرد. |
| `SCM_HPS_REST_INSECURE_SSL` | SSL ناامن | اگر `true` باشد اعتبارسنجی SSL ضعیف میشود. در pilot/prod نباید بدون تایید امنیت فعال شود. |
| `SCM_HPS_REST_MAX_BODY_LOG_LENGTH` | سقف log بدنه | سقف طول بدنه امن قابل log شدن است. مقدار زیاد میتواند خطر نشت داده ایجاد کند. |
| `SCM_HPS_REST_SENSITIVE_HEADERS` | headerهای حساس | فهرست headerهایی است که باید mask شوند. این فهرست نباید ضعیف شود. |
| `SCM_HPS_REST_SENSITIVE_BODY_KEYS` | کلیدهای حساس بدنه | فهرست کلیدهایی است که در body باید mask شوند، مثل password، token، pin و card. |
| `SCM_HPS_REST_RATE_LIMIT_ENABLED` | rate limit | فعال بودن rate limit برای HPS REST است. |
| `SCM_HPS_REST_RATE_LIMIT_BUCKET` | bucket rate limit | نام bucket مربوط به rate limit HPS REST است. |
| `SCM_HPS_REST_RATE_LIMIT_KEY` | کلید rate limit | کلید rate limit HPS REST است. |

## 10. متغیرهای HPS Shetab7 Provider

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_HPS_SHETAB7_ENABLED` | فعال بودن Shetab7 | فعال یا غیرفعال بودن provider Shetab7 را مشخص میکند. |
| `SCM_HPS_SHETAB7_SCHEME` | scheme Shetab7 | scheme داخلی provider است. معمولاً `scm-shetab` است. |
| `SCM_HPS_SHETAB7_ENDPOINTS` | endpointهای Shetab7 | فهرست endpointهای TCP/ISO Shetab7 است. مقدارها با کاما جدا میشوند. |
| `SCM_HPS_SHETAB7_CONNECT_TIMEOUT_MS` | timeout اتصال Shetab7 | مقدار اختصاصی connect timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_CONNECT_TIMEOUT_MS` استفاده میشود. |
| `SCM_HPS_SHETAB7_SOCKET_TIMEOUT_MS` | timeout socket Shetab7 | مقدار اختصاصی socket timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_SOCKET_TIMEOUT_MS` استفاده میشود. |
| `SCM_HPS_SHETAB7_SEND_TIMEOUT_MS` | timeout ارسال Shetab7 | مقدار اختصاصی send timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_SEND_TIMEOUT_MS` استفاده میشود. |
| `SCM_HPS_SHETAB7_RESPONSE_TIMEOUT_MS` | timeout پاسخ Shetab7 | مقدار اختصاصی response timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_RESPONSE_TIMEOUT_MS` استفاده میشود. |
| `SCM_HPS_SHETAB7_QUEUE_CAPACITY` | ظرفیت صف Shetab7 | ظرفیت صف provider است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_QUEUE_CAPACITY` استفاده میشود. |
| `SCM_HPS_SHETAB7_PIN_KEY` | کلید PIN | کلید رمزنگاری PIN است و کاملاً حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_HPS_SHETAB7_MAC_KEY` | کلید MAC | کلید MAC است و کاملاً حساس است. در pilot/prod باید از Secret Management بیاید. |
| `SCM_DEV_HPS_SHETAB7_PIN_KEY` | کلید PIN محیط dev | فقط fallback محیط dev برای `SCM_HPS_SHETAB7_PIN_KEY` است. برای test/pilot/prod استفاده نشود. |
| `SCM_DEV_HPS_SHETAB7_MAC_KEY` | کلید MAC محیط dev | فقط fallback محیط dev برای `SCM_HPS_SHETAB7_MAC_KEY` است. برای test/pilot/prod استفاده نشود. |
| `SCM_HPS_SHETAB7_PACKAGER_CLASS` | packager پیام | کلاس packager پیام Shetab7 است. معمولاً نباید توسط پشتیبانی تغییر کند. |
| `SCM_HPS_SHETAB7_RECONNECT_DELAY_MS` | فاصله reconnect | فاصله تلاش reconnect بر حسب میلی ثانیه است. |
| `SCM_HPS_SHETAB7_SAME_ENDPOINT_RECONNECT_ATTEMPTS` | تعداد reconnect روی endpoint | تعداد تلاش مجدد روی همان endpoint است. |
| `SCM_HPS_SHETAB7_ENDPOINT_LEASE_ENABLED` | lease endpoint | فعال بودن lease endpoint برای مدیریت endpointهای Shetab7 است. |
| `SCM_HPS_SHETAB7_ENDPOINT_LEASE_TTL_MS` | زمان lease endpoint | مدت اعتبار lease endpoint بر حسب میلی ثانیه است. |
| `SCM_HPS_SHETAB7_RATE_LIMIT_ENABLED` | rate limit | فعال بودن rate limit برای Shetab7 است. |
| `SCM_HPS_SHETAB7_RATE_LIMIT_BUCKET` | bucket rate limit | نام bucket مربوط به rate limit Shetab7 است. |
| `SCM_HPS_SHETAB7_RATE_LIMIT_KEY` | کلید rate limit | کلید rate limit Shetab7 است. |
| `SCM_HPS_SHETAB7_EXPIRY_CUSTOMIZER_TYPE` | customizer تاریخ انقضا | نوع customizer تاریخ انقضای کارت است. |
| `SCM_HPS_SHETAB7_EXPIRY_FIELD` | فیلد تاریخ انقضا | شماره فیلد ISO مربوط به تاریخ انقضا است. |
| `SCM_HPS_SHETAB7_EXPIRY_SOURCE` | منبع تاریخ انقضا | مسیر مقدار تاریخ انقضا در پیام داخلی است. |
| `SCM_HPS_SHETAB7_CVV2_CUSTOMIZER_TYPE` | customizer CVV2 | نوع customizer مربوط به CVV2 است. |
| `SCM_HPS_SHETAB7_CVV2_FIELD` | فیلد CVV2 | شماره فیلد ISO مربوط به CVV2 است. |
| `SCM_HPS_SHETAB7_CVV2_TAG` | tag CVV2 | tag داخلی CVV2 در پیام است. |
| `SCM_HPS_SHETAB7_CVV2_SOURCE` | منبع CVV2 | مسیر مقدار CVV2 در پیام داخلی است و نباید در log خام نمایش داده شود. |
| `SCM_HPS_SHETAB7_CVV2_LENGTH_DIGITS` | رقمهای طول CVV2 | تعداد رقمهای طول CVV2 در پیام است. |
| `SCM_HPS_SHETAB7_CVV2_MIN_LENGTH` | حداقل طول CVV2 | حداقل طول مجاز CVV2 است. |
| `SCM_HPS_SHETAB7_CVV2_MAX_LENGTH` | حداکثر طول CVV2 | حداکثر طول مجاز CVV2 است. |
| `SCM_HPS_SHETAB7_PIN_BLOCK_CUSTOMIZER_TYPE` | customizer PIN block | نوع customizer ساخت PIN block است. |
| `SCM_HPS_SHETAB7_PIN_FIELD` | فیلد PIN | شماره فیلد ISO مربوط به PIN block است. |
| `SCM_HPS_SHETAB7_PAN_FIELD` | فیلد PAN | شماره فیلد ISO مربوط به PAN است. PAN داده حساس است و نباید خام ثبت شود. |
| `SCM_HPS_SHETAB7_PIN_SOURCE` | منبع PIN | مسیر مقدار PIN در پیام داخلی است و داده حساس محسوب میشود. |
| `SCM_HPS_SHETAB7_MAC_CUSTOMIZER_TYPE` | customizer MAC | نوع customizer مربوط به MAC است. |
| `SCM_HPS_SHETAB7_MAC_FIELD` | فیلد MAC | شماره فیلد ISO مربوط به MAC است. |
| `SCM_HPS_SHETAB7_MAC_VERIFY_RESPONSE` | بررسی MAC پاسخ | مشخص میکند MAC پاسخ بررسی شود یا نه. |

## 11. متغیرهای API Repo Provider

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_API_REPO_ENABLED` | فعال بودن API Repo | فعال یا غیرفعال بودن API Repo provider را مشخص میکند. |
| `SCM_API_REPO_BASE_URL` | آدرس API Repo | آدرس پایه API Repo است. در test ممکن است به سرویس تست اشاره کند. |
| `SCM_API_REPO_SCHEMA` | schema API Repo | نام schema یا شناسه تنظیم API Repo است. |
| `SCM_API_REPO_CONNECT_TIMEOUT_MS` | timeout اتصال API Repo | مقدار اختصاصی connect timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_CONNECT_TIMEOUT_MS` استفاده میشود. |
| `SCM_API_REPO_RESPONSE_TIMEOUT_MS` | timeout پاسخ API Repo | مقدار اختصاصی response timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_RESPONSE_TIMEOUT_MS` استفاده میشود. |
| `SCM_API_REPO_VIRTUAL_THREADS_ENABLED` | virtual thread | فعال بودن virtual thread برای API Repo است. |
| `SCM_API_REPO_FOLLOW_REDIRECTS` | دنبال کردن redirect | سیاست follow redirect برای API Repo است. |
| `SCM_API_REPO_INSECURE_SSL` | SSL ناامن API Repo | اگر `true` باشد اعتبارسنجی SSL ضعیف میشود. در pilot/prod بدون تایید امنیت فعال نشود. |
| `SCM_API_REPO_DEFAULT_METHOD` | متد پیش فرض | متد HTTP پیش فرض برای provider است. مقدار رایج `POST` است. |
| `SCM_API_REPO_ACCEPT_HEADER` | header Accept | مقدار header `Accept` برای API Repo است. |
| `SCM_API_REPO_CONTENT_TYPE_HEADER` | header Content-Type | مقدار header `Content-Type` برای API Repo است. |

## 12. متغیرهای NAB ATPS Provider

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_NAB_ATPS_ENABLED` | فعال بودن NAB ATPS | فعال یا غیرفعال بودن provider را مشخص میکند. |
| `SCM_NAB_ATPS_SCHEME` | scheme NAB | scheme داخلی provider است. معمولاً `scm-nab` است. |
| `SCM_NAB_ATPS_ENDPOINT` | endpoint NAB ATPS | آدرس و پورت سرویس ATPS است. در test ممکن است به endpoint تست اشاره کند. |
| `SCM_NAB_ATPS_CONNECT_TIMEOUT_MS` | timeout اتصال NAB | مقدار اختصاصی connect timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_CONNECT_TIMEOUT_MS` استفاده میشود. |
| `SCM_NAB_ATPS_SOCKET_TIMEOUT_MS` | timeout socket NAB | مقدار اختصاصی socket timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_SOCKET_TIMEOUT_MS` استفاده میشود. |
| `SCM_NAB_ATPS_RESPONSE_TIMEOUT_MS` | timeout پاسخ NAB | مقدار اختصاصی response timeout است. اگر تنظیم نشود، از `SCM_PROVIDER_DEFAULT_RESPONSE_TIMEOUT_MS` استفاده میشود. |
| `SCM_NAB_ATPS_USER_ID` | شناسه کاربر NAB | user id اتصال به NAB ATPS است. |
| `SCM_NAB_ATPS_PASSWORD` | رمز NAB | رمز provider است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. در pilot/prod باید از Secret Management بیاید. |
| `SCM_DEV_NAB_ATPS_PASSWORD` | رمز dev NAB | فقط fallback محیط dev برای `SCM_NAB_ATPS_PASSWORD` است و نباید برای test/pilot/prod استفاده شود. |
| `SCM_NAB_ATPS_PROTOCOL` | پروتکل NAB | نام پروتکل ATPS است. معمولاً `ATPS` است. |
| `SCM_NAB_ATPS_ACK_LENGTH_BYTES` | طول ACK | طول ACK بر حسب byte است. معمولاً نباید تغییر کند. |
| `SCM_NAB_ATPS_CHARSET` | charset | charset پیامهای NAB است. مقدار رایج `windows-1256` است. |
| `SCM_NAB_ATPS_RQ_UID_LENGTH` | طول RQ UID | طول شناسه درخواست است. |
| `SCM_NAB_ATPS_RQ_UID_TYPE` | نوع RQ UID | نوع شناسه درخواست است. مقدار رایج `NUMERIC` است. |
| `SCM_NAB_ATPS_RATE_LIMIT_ENABLED` | rate limit | فعال بودن rate limit برای NAB ATPS است. |
| `SCM_NAB_ATPS_RATE_LIMIT_BUCKET` | bucket rate limit | نام bucket مربوط به rate limit NAB ATPS است. |
| `SCM_NAB_ATPS_RATE_LIMIT_KEY` | کلید rate limit | کلید rate limit NAB ATPS است. |
| `SCM_NAB_ATPS_HEADER_PROTOCOL_NAME` | نام فیلد protocol | نام فیلد protocol در header ATPS است. معمولاً تغییر نمیکند. |
| `SCM_NAB_ATPS_HEADER_PROTOCOL_LENGTH` | طول فیلد protocol | طول فیلد protocol در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_PROTOCOL_REQUIRED` | اجباری بودن protocol | مشخص میکند فیلد protocol اجباری است یا نه. |
| `SCM_NAB_ATPS_HEADER_COMMAND_NAME` | نام فیلد command | نام فیلد command در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_COMMAND_LENGTH` | طول فیلد command | طول فیلد command در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_COMMAND_REQUIRED` | اجباری بودن command | مشخص میکند فیلد command اجباری است یا نه. |
| `SCM_NAB_ATPS_HEADER_SERVICE_CODE_NAME` | نام فیلد serviceCode | نام فیلد serviceCode در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_SERVICE_CODE_LENGTH` | طول فیلد serviceCode | طول فیلد serviceCode در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_SERVICE_CODE_REQUIRED` | اجباری بودن serviceCode | مشخص میکند فیلد serviceCode اجباری است یا نه. |
| `SCM_NAB_ATPS_HEADER_DATE_TIME_NAME` | نام فیلد dateTime | نام فیلد dateTime در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_DATE_TIME_LENGTH` | طول فیلد dateTime | طول فیلد dateTime در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_DATE_TIME_REQUIRED` | اجباری بودن dateTime | مشخص میکند فیلد dateTime اجباری است یا نه. |
| `SCM_NAB_ATPS_HEADER_USER_ID_NAME` | نام فیلد userId | نام فیلد userId در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_USER_ID_LENGTH` | طول فیلد userId | طول فیلد userId در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_USER_ID_REQUIRED` | اجباری بودن userId | مشخص میکند فیلد userId اجباری است یا نه. |
| `SCM_NAB_ATPS_HEADER_PASSWORD_NAME` | نام فیلد password | نام فیلد password در header ATPS است. مقدار password حساس است و نباید خام ثبت شود. |
| `SCM_NAB_ATPS_HEADER_PASSWORD_LENGTH` | طول فیلد password | طول فیلد password در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_PASSWORD_REQUIRED` | اجباری بودن password | مشخص میکند فیلد password اجباری است یا نه. |
| `SCM_NAB_ATPS_HEADER_RQ_UID_NAME` | نام فیلد rqUid | نام فیلد rqUid در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_RQ_UID_LENGTH` | طول فیلد rqUid | طول فیلد rqUid در header ATPS است. |
| `SCM_NAB_ATPS_HEADER_RQ_UID_REQUIRED` | اجباری بودن rqUid | مشخص میکند فیلد rqUid اجباری است یا نه. |

## 13. مواردی که نباید دوباره استفاده شوند

- profileهای قدیمی مثل `ib4dev`، `scm4dev`، `ib4test`، `scm4test`، `mb4dev`، `mb4test`، `pwa4dev` و `pwa4test` نباید دوباره ساخته یا فعال شوند.
- گروه `scm.log.*` نباید استفاده شود. جایگزین آن `scm.observation.*` است.
- گروه `scm.logging.datasource.*` نباید در `scm-web` تعریف شود. log، trace و audit باید از مسیر Observation بروند.
- گروه `scm.datasource.secondary.*` نباید در `scm-web` تعریف شود. این برنامه فقط یک دیتابیس اصلی با `scm.datasource.primary` دارد.
- secretها، passwordها، tokenها، PIN، CVV2، MAC key و اطلاعات کارت نباید در فایل config، ticket، screenshot یا پیام chat قرار بگیرند.

## 14. نکات خاص هر محیط

- در `dev` مقدارها معمولاً local یا test هستند و Config Server غیرفعال است. مقدارهای password و key فقط placeholder امن یا مقدار توسعه باشند.
- در `test` مقدارها از Config Server می آیند و ممکن است به سرویسهای تست اشاره کنند. اگر Config Server در دسترس نباشد، سرویس باید fail-fast کند.
- در `pilot` و `prod` secretها نباید hardcode شوند و باید از Secret Management یا مسیر امن سازمانی بیایند.
- برای `pilot` و `prod` از `optional:configserver:` استفاده نمیشود؛ اگر Config Server در دسترس نباشد، سرویس نباید با config ناقص بالا بیاید.
- برای تغییر کانال یا نمونه، profile جدید نسازید. مقدارهای `SCM_CHANNEL_CODE`، `SCM_GATEWAY_NAME` و در صورت نیاز `SCM_GATEWAY_NAMES` را تغییر دهید.
