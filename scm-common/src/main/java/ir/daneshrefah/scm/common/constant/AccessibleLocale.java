package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum AccessibleLocale {
    DEFAULT_LOCALE("en", "US",new Locale("en","US")),
    /*-----------------------------------------------------------------------------------*/
    FA_IR("fa", "IR",new Locale("fa","IR")),
    AR_AE("ar", "AE",new Locale("ar","AE")),
    EN_US("en", "US",new Locale("en","US"));

    private final String languageCode;
    private final String countryCode;
    private final Locale locale;

    public static Optional<AccessibleLocale> findByLocale(java.util.Locale locale) {
        if (Objects.nonNull(locale)) {
            String country = locale.getCountry();
            String language = locale.getLanguage();
            return Arrays.stream(values()).filter(accessibleLocales -> accessibleLocales.getCountryCode().equals(country))
                    .filter(accessibleLocales -> accessibleLocales.getLanguageCode().equals(language))
                    .findFirst();
        }
        return Optional.empty();
    }

    public static Optional<AccessibleLocale> findByLocale(String locale) {
        if (Objects.nonNull(locale)) {
            String splitter = "_";
            if (locale.contains("-")){
                splitter = "-";
            }
            String[] localeSplit = locale.split(splitter);
            String language = localeSplit[0];
            String country = localeSplit[1];
            return Arrays.stream(values())
                    .filter(accessibleLocales -> accessibleLocales.getCountryCode().equals(country))
                    .filter(accessibleLocales -> accessibleLocales.getLanguageCode().equals(language))
                    .findFirst();
        }
        return Optional.empty();
    }

}
