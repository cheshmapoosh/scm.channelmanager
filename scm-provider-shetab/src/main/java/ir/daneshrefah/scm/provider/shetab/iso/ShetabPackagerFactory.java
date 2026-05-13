package ir.daneshrefah.scm.provider.shetab.iso;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO87APackager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class ShetabPackagerFactory {
    private final ResourceLoader resourceLoader;

    public ShetabPackagerFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ISOPackager create(ShetabResolvedConfig config) {
        if (StringUtils.isNotBlank(config.packagerXml())) {
            return createGenericPackager(config.packagerXml());
        }
        if (StringUtils.isNotBlank(config.packagerClass())) {
            return createPackagerByClass(config.packagerClass());
        }
        return new ISO87APackager();
    }

    private ISOPackager createGenericPackager(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            try (InputStream inputStream = resource.getInputStream()) {
                return new GenericPackager(inputStream);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create jPOS GenericPackager from " + location, e);
        }
    }

    private ISOPackager createPackagerByClass(String className) {
        try {
            Class<?> packagerClass = Class.forName(className);
            Object instance = packagerClass.getDeclaredConstructor().newInstance();
            if (!(instance instanceof ISOPackager packager)) {
                throw new IllegalArgumentException(className + " is not an ISOPackager");
            }
            return packager;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate jPOS packager " + className, e);
        }
    }
}
