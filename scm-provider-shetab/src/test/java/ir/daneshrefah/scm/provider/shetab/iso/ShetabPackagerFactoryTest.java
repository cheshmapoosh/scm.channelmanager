package ir.daneshrefah.scm.provider.shetab.iso;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.packager.Shetab7AsciiXAPackager;
import ir.daneshrefah.scm.provider.shetab.iso.packager.Shetab7BinaryXAPackager;
import org.jpos.iso.ISOPackager;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ShetabPackagerFactoryTest {

    private final ShetabPackagerFactory factory = new ShetabPackagerFactory(new DefaultResourceLoader());

    @Test
    void resolvesShetab7AsciiPackagerBySimpleClassName() {
        ISOPackager packager = factory.create(config("Shetab7AsciiXAPackager", null));

        assertInstanceOf(Shetab7AsciiXAPackager.class, packager);
    }

    @Test
    void resolvesShetab7BinaryPackagerByLegacyClassName() {
        ISOPackager packager = factory.create(config("ir.dpi.cm.core.card.packager.CardSystemBinaryXAPackager", null));

        assertInstanceOf(Shetab7BinaryXAPackager.class, packager);
    }

    private static ShetabResolvedConfig config(String packagerClass, String packagerXml) {
        return new ShetabResolvedConfig(
                "poya",
                List.of("10.10.10.10:9000"),
                packagerClass,
                packagerXml,
                3000,
                1000,
                6000,
                1000,
                1000,
                3,
                1000,
                new ShetabResolvedConfig.RateLimit(false, "unused", "provider"),
                new ShetabResolvedConfig.EndpointLease(false, 30_000L),
                new ShetabResolvedConfig.Security(
                        new ShetabResolvedConfig.Pin(false, null, 52, 2),
                        new ShetabResolvedConfig.Mac(false, null, 128, false, "AAAAAAAAAAAAAAAA", 16)
                )
        );
    }
}
