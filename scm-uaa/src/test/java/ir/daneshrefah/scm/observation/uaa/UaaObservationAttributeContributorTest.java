package ir.daneshrefah.scm.observation.uaa;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.ObservationAttributeSensitivity;
import ir.daneshrefah.scm.observation.ObservationStream;
import ir.daneshrefah.scm.uaa.observation.UaaObservationAttributeContributor;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaTraceAttributes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UaaObservationAttributeContributorTest {
    @Test
    void contributorRegistersUaaLogAndTraceAttributes() {
        ObservationAttributeRegistry registry = registry();

        assertTrue(registry.contains(ObservationStream.LOG, "uaa.auth.type"));
        assertTrue(registry.contains(ObservationStream.LOG, "uaa.jwt.masked"));
        assertTrue(registry.contains(ObservationStream.TRACE, "uaa.auth.type"));
        assertTrue(registry.contains(ObservationStream.TRACE, "uaa.jwt.masked"));
    }

    @Test
    void jwtMaskingUsesFiveVisibleCharactersOnBothSides() {
        ObservationAttributeRegistry registry = registry();

        assertEquals("abcde...vwxyz",
                registry.prepareValue(ObservationStream.TRACE, "uaa.jwt.masked", "abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    void usernameAndSubjectAreRaw() {
        ObservationAttributeKey<?> username = UaaTraceAttributes.JWT_USERNAME;
        ObservationAttributeKey<?> subject = UaaTraceAttributes.JWT_SUBJECT;

        assertEquals(ObservationAttributeSensitivity.RAW, username.sensitivity());
        assertEquals(ObservationAttributeSensitivity.RAW, subject.sensitivity());
    }

    private ObservationAttributeRegistry registry() {
        return new ObservationAttributeRegistry(List.of(new UaaObservationAttributeContributor()));
    }
}
