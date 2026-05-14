package ir.daneshrefah.scm.provider.shetab.iso;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.packager.Shetab7AsciiXAPackager;
import ir.daneshrefah.scm.provider.shetab.iso.packager.Shetab7BinaryXAPackager;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class ShetabPackagerFactory {
    private static final Map<String, Supplier<ISOPackager>> BUILTIN_PACKAGERS = Map.ofEntries(
            Map.entry("Shetab7AsciiXAPackager", Shetab7AsciiXAPackager::new),
            Map.entry("Shetab7BinaryXAPackager", Shetab7BinaryXAPackager::new),
            Map.entry(Shetab7AsciiXAPackager.class.getName(), Shetab7AsciiXAPackager::new),
            Map.entry(Shetab7BinaryXAPackager.class.getName(), Shetab7BinaryXAPackager::new),
            Map.entry("CardSystemAsciiXAPackager", Shetab7AsciiXAPackager::new),
            Map.entry("CardSystemBinaryXAPackager", Shetab7BinaryXAPackager::new),
            Map.entry("ir.dpi.cm.core.card.packager.CardSystemAsciiXAPackager", Shetab7AsciiXAPackager::new),
            Map.entry("ir.dpi.cm.core.card.packager.CardSystemBinaryXAPackager", Shetab7BinaryXAPackager::new)
    );

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
        throw new IllegalArgumentException("Packager not configured. set packagerXml or packagerClass");
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
        String normalizedClassName = StringUtils.trimToEmpty(className);
        Supplier<ISOPackager> builtinSupplier = BUILTIN_PACKAGERS.get(normalizedClassName);
        if (builtinSupplier != null) {
            return builtinSupplier.get();
        }

        try {
            Class<?> packagerClass = Class.forName(normalizedClassName);
            Object instance = packagerClass.getDeclaredConstructor().newInstance();
            if (!(instance instanceof ISOPackager packager)) {
                throw new IllegalArgumentException(normalizedClassName + " is not an ISOPackager");
            }
            return packager;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate jPOS packager " + normalizedClassName, e);
        }
    }
}
