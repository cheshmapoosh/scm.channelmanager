package ir.daneshrefah.scm.common.error.management;

import ir.daneshrefah.scm.common.constant.BundleParameterPattern;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.BundleDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ir.daneshrefah.scm.common.constant.BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_PREFIX;

/**
 * @see BundleDefaults ExceptionBundleGrammer
 */
@Component
@RequiredArgsConstructor
public class ExceptionMessageBundleProvider {
    private final ResourceBundleService resourceBundleService;


    Map<String, java.util.Locale> LOCALE_CACHE = new ConcurrentHashMap<>();

    public String getDefaultExceptionMessage(java.util.Locale locale){
        return resourceBundleService.get(locale, BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_KEY).orElse("");
    }

    public String getDefaultExceptionMessage(AccessibleLocale locale){
        return resourceBundleService.get(getLocaleFromCache(locale), BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_KEY).orElse("");
    }

    public String getExceptionMessage(java.util.Locale locale, String exceptionMessageKey){
        return resourceBundleService.get(locale, exceptionMessageKey).orElse("");
    }

    public String getExceptionMessage(java.util.Locale locale, String exceptionMessageKey,Map<String,String> parameters){
        return resourceBundleService.get(locale, exceptionMessageKey, BundleParameterPattern.KEY_ASSIGNMENT,parameters).orElse("");
    }

    public String getExceptionMessage(java.util.Locale locale, Throwable throwable,Map<String,String> parameters){
        String key = EXCEPTION_BUNDLE_DEFAULT_PREFIX + throwable.getClass().getName();
        return getExceptionMessage(locale,key,parameters);
    }

    public String getExceptionMessage(java.util.Locale locale, Throwable throwable){
        String key = EXCEPTION_BUNDLE_DEFAULT_PREFIX + throwable.getClass().getName();
        return getExceptionMessage(locale,key);
    }

    public String getExceptionMessage(java.util.Locale locale, Throwable throwable,String overrideName,Map<String,String> parameters){
        String key = EXCEPTION_BUNDLE_DEFAULT_PREFIX + throwable.getClass().getName();
        return getExceptionMessage(locale,key,parameters);
    }

    public String getDynamicExceptionMessage(java.util.Locale locale, String bundleKey,Map<String,String> parameters){
        return resourceBundleService.get(locale,bundleKey,BundleParameterPattern.KEY_ASSIGNMENT,parameters).orElse("");
    }

    public String getExceptionMessage(AccessibleLocale locale, String exceptionMessageKey){
        return resourceBundleService.get(getLocaleFromCache(locale), exceptionMessageKey).orElse("");
    }

    public String getExceptionMessage(AccessibleLocale locale, Throwable throwable){
        String key = EXCEPTION_BUNDLE_DEFAULT_PREFIX + throwable.getClass().getName();
        return resourceBundleService.get(getLocaleFromCache(locale), key).orElse("");
    }

    public java.util.Locale getLocaleFromCache(String languageCode, String countryCode){
        String key = languageCode+"-"+countryCode;
        return LOCALE_CACHE.computeIfAbsent(key,s -> new java.util.Locale(languageCode,countryCode));
    }

    public java.util.Locale getLocaleFromCache(AccessibleLocale accessibleLocales){
        return getLocaleFromCache(accessibleLocales.getLanguageCode(),accessibleLocales.getCountryCode());
    }
}
