package ir.daneshrefah.scm.process.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@PropertySource("classpath:process.properties")
public class ProcessBundleService {

    @Autowired
    private Environment env;

    private final Map<String, String> bundleCache = new ConcurrentHashMap<>();

    public String getBundle(String key) {
        if (key == null) return "";
        if (bundleCache.containsKey(key)) {
            return bundleCache.get(key);
        }
        String property = env.getProperty(key);
        if (property != null && !property.trim().isEmpty()) {
            bundleCache.put(key, property);
        }
        return property;
    }
}
