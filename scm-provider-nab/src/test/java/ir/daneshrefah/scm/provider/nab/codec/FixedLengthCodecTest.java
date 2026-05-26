package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedLengthCodecTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonFieldSpecReader specReader = new JsonFieldSpecReader();
    private final NabValueConverterRegistry converterRegistry = new NabValueConverterRegistry();
    private final FixedLengthEncoder encoder = new FixedLengthEncoder(converterRegistry);
    private final FixedLengthDecoder decoder = new FixedLengthDecoder(objectMapper, converterRegistry);

    @Test
    void fieldDefaultsAreStringAndNotRequiredAndPathFallsBackToName() throws Exception {
        List<NabFieldSpec> fields = specReader.readFields(objectMapper.readTree("""
                [
                  {"name": "customerId", "length": 12},
                  {"name": "generalAccount", "length": 4}
                ]
                """), "request");

        ObjectNode data = objectMapper.createObjectNode();
        data.put("customerId", "123456");

        assertEquals("123456          ", encoder.encode(data, fields));
    }

    @Test
    void nameAndLengthAreRequired() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> specReader.readFields(objectMapper.readTree("""
                [{"length": 12}]
                """), "request"));
        assertThrows(IllegalArgumentException.class, () -> specReader.readFields(objectMapper.readTree("""
                [{"name": "customerId"}]
                """), "request"));
    }

    @Test
    void supportsTypeConverterPaddingAndOverflowPolicy() throws Exception {
        List<NabFieldSpec> fields = specReader.readFields(objectMapper.readTree("""
                [
                  {"name": "amount", "length": 6, "type": "NUMBER", "padding": "LEFT_ZERO", "required": true},
                  {"name": "flag", "length": 1, "type": "BOOLEAN"}
                ]
                """), "request");

        ObjectNode data = objectMapper.createObjectNode();
        data.put("amount", "123");
        data.put("flag", true);

        assertEquals("0001231", encoder.encode(data, fields));
    }

    @Test
    void decodesTypedResponseFields() throws Exception {
        List<NabFieldSpec> fields = specReader.readFields(objectMapper.readTree("""
                [
                  {"name": "amount", "length": 6, "type": "NUMBER"},
                  {"name": "flag", "length": 1, "type": "BOOLEAN"}
                ]
                """), "response");

        ObjectNode decoded = decoder.decode("0001231", fields);

        assertEquals(123L, decoded.get("amount").asLong());
        assertEquals(true, decoded.get("flag").asBoolean());
    }
}
