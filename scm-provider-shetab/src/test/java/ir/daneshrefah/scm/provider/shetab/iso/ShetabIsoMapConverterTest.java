package ir.daneshrefah.scm.provider.shetab.iso;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShetabIsoMapConverterTest {

    @Test
    void convertsCanonicalMapToIsoMsg() throws Exception {
        ShetabIsoMapConverter converter = new ShetabIsoMapConverter();

        ISOMsg msg = converter.toIsoMsg(Map.of(
                "mti", "1100",
                "fields", Map.of(
                        "2", "5894631150168490",
                        "3", "330000",
                        "11", "123456"
                )
        ));

        assertEquals("1100", msg.getMTI());
        assertEquals("5894631150168490", msg.getString(2));
        assertEquals("330000", msg.getString(3));
        assertEquals("123456", msg.getString(11));
    }

    @Test
    void convertsIsoMsgToCanonicalMap() throws Exception {
        ShetabIsoMapConverter converter = new ShetabIsoMapConverter();
        ISOMsg msg = new ISOMsg();
        msg.setMTI("1110");
        msg.set(39, "00");
        msg.set(11, "123456");

        Map<String, Object> body = converter.toMap(msg);
        Map<?, ?> fields = (Map<?, ?>) body.get("fields");

        assertEquals("1110", body.get("mti"));
        assertEquals("00", fields.get("39"));
        assertEquals("123456", fields.get("11"));
    }
}
