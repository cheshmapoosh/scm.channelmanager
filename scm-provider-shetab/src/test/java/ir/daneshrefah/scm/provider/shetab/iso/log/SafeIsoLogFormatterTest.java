package ir.daneshrefah.scm.provider.shetab.iso.log;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeIsoLogFormatterTest {

    @Test
    void redactsUnknownFieldsAndKeepsSafeFieldsMaskedOrVisible() throws Exception {
        ISOMsg msg = new ISOMsg();
        msg.setMTI("1100");
        msg.set(2, "5894631150168490");
        msg.set(3, "330000");
        msg.set(11, "123456");
        msg.set(37, "691199261655");
        msg.set(48, "PRIVATE-PROVIDER-DATA");
        msg.set(52, "0123456789ABCDEF");

        String logLine = SafeIsoLogFormatter.format(msg);

        assertTrue(logLine.contains("mti=1100"));
        assertTrue(logLine.contains("2=589463******8490"));
        assertTrue(logLine.contains("3=330000"));
        assertTrue(logLine.contains("11=123456"));
        assertTrue(logLine.contains("37=691199261655"));
        assertTrue(logLine.contains("48=[REDACTED]"));
        assertTrue(logLine.contains("52=[HIDDEN,len=16]"));
        assertFalse(logLine.contains("PRIVATE-PROVIDER-DATA"));
    }
}
