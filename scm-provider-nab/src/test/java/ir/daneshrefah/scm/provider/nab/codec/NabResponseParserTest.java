package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.provider.nab.domain.NabResponseSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NabResponseParserTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonFieldSpecReader fieldReader = new JsonFieldSpecReader();
    private final NabResponseSpecReader responseSpecReader = new NabResponseSpecReader(fieldReader);
    private final NabResponseParser parser = new NabResponseParser(
            objectMapper,
            new FixedLengthDecoder(objectMapper, new NabValueConverterRegistry())
    );

    @Test
    void parsesSuccessfulSingleResponseWithoutRaw() throws Exception {
        NabResponseSpec spec = responseSpecReader.read(objectMapper.readTree("""
                {
                  "response": {
                    "fields": [
                      {"name": "command", "length": 2},
                      {"name": "accountNo", "length": 18, "converter": "TRIM"}
                    ]
                  }
                }
                """));

        ObjectNode result = parser.parse("0000027000000000000123456", spec);

        assertTrue(result.get("status").get("success").asBoolean());
        assertFalse(result.has("raw"));
        assertEquals("27", result.get("data").get("command").asText());
        assertEquals("000000000000123456", result.get("data").get("accountNo").asText());
    }

    @Test
    void parsesSuccessfulListResponseWhereEachLineHasListActionCode() throws Exception {
        NabResponseSpec spec = responseSpecReader.read(objectMapper.readTree("""
                {
                  "response": {
                    "recordSeparator": "\\n",
                    "fields": [
                      {"name": "accountNo", "length": 18},
                      {"name": "accountType", "length": 2}
                    ]
                  }
                }
                """));

        ObjectNode result = parser.parse(
                "1000000000000000012345610\n"
                        + "1000000000000000065432120", spec);

        assertTrue(result.get("status").get("success").asBoolean());
        assertTrue(result.get("status").get("list").asBoolean());
        assertEquals(2, result.get("records").size());
        assertEquals("000000000000123456", result.get("records").get(0).get("accountNo").asText());
        assertEquals("20", result.get("records").get(1).get("accountType").asText());
    }

    @Test
    void unsuccessfulResponseReturnsOnlyStatus() throws Exception {
        NabResponseSpec spec = responseSpecReader.read(objectMapper.readTree("""
                {
                  "response": {
                    "fields": [
                      {"name": "accountNo", "length": 18}
                    ]
                  }
                }
                """));

        ObjectNode result = parser.parse("12345", spec);

        assertFalse(result.get("status").get("success").asBoolean());
        assertFalse(result.has("data"));
        assertFalse(result.has("records"));
        assertFalse(result.has("raw"));
    }
}
