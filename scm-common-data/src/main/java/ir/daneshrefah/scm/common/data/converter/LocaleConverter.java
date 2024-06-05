package ir.daneshrefah.scm.common.data.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Converter
public class LocaleConverter implements AttributeConverter<Locale, String> {

    Map<String, Locale> LOCALE_CACHE = new ConcurrentHashMap<>();

    @Override
    public String convertToDatabaseColumn(Locale locale) {
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    @Override
    public Locale convertToEntityAttribute(String locale) {
        return LOCALE_CACHE.computeIfAbsent(locale, key -> {
            String[] split = locale.split("-");
            String languageCode = split[0];
            String countryCode = split[1];
            return new Locale(languageCode, countryCode);
        });
    }
}
