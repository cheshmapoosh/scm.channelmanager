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

    @Test
    void parsesLegacyActiveAccountsSampleFromRealLog() throws Exception {
        NabResponseSpec spec = responseSpecReader.read(objectMapper.readTree("""
                {
                  "response": {
                    "recordSeparator": "\\n",
                    "status": {
                      "field": {"name": "actionCode", "length": 5},
                      "successCode": "00000",
                      "successListCode": "10000"
                    },
                    "fields": [
                      {"name": "command", "length": 2},
                      {"name": "service", "length": 2},
                      {"name": "dateTime", "length": 14},
                      {"name": "accountNo", "length": 18},
                      {"name": "accountType", "length": 2},
                      {"name": "accountDesc", "length": 60},
                      {"name": "accountLedgerBalance", "length": 18},
                      {"name": "accountAvailableBalance", "length": 18},
                      {"name": "branchNo", "length": 6},
                      {"name": "customerId", "length": 12},
                      {"name": "customerType", "length": 2},
                      {"name": "sharingStatus", "length": 1},
                      {"name": "rqUid", "length": 16},
                      {"name": "lastUpdateTimeMs", "length": 17},
                      {"name": "microSec", "length": 3},
                      {"name": "accountStatus", "length": 2},
                      {"name": "accountBlockedAmount", "length": 18},
                      {"name": "accountIban", "length": 30},
                      {"name": "commercial", "length": 1},
                      {"name": "generalAccount", "length": 8},
                      {"name": "generalAccountDesc", "length": 60},
                      {"name": "subsidiary", "length": 8},
                      {"name": "subsidiaryDesc", "length": 60},
                      {"name": "isColorMoney", "length": 1}
                    ]
                  }
                }
                """));

        String response = """
                1000027251404082113401400000000003572406710                                                            0000000000404065760000000000404065760001010000037821735001244422         2025110909231921868700000000000000000000IR920130100000000035724067    000000409سپرده  قرض  الحسنه  پس انداز                                00000001مشتریان                                                     
                1000027251404082113401400000000003572419530محدودیت مشاهده مانده                                        0000000000402458330000000000402458330001010000037821735001244422         2025110909203151720800000000000000000000IR310130100000000035724195    000000414سپرده سرمایه گذاری ویژه                                     00000034سپرده سرمایه گذاری بلندمدت یکساله کارکنان بانک (ص ب د       
                1000027251404082113401400000000039980394430                                                            0000000000000000000000000000000000000001010000037821735001244422         2025111009181686643200000000000000000000IR500130100000000399803944    000004110سپرده سرمایه گذاری کوتاه مدت                                00000228طرح امید رفاه 1                                             
                """;

        ObjectNode result = parser.parse(response, spec);

        assertTrue(result.get("status").get("success").asBoolean());
        assertTrue(result.get("status").get("list").asBoolean());
        assertEquals(3, result.get("records").size());
        assertEquals("27", result.get("records").get(0).get("command").asText());
        assertEquals("25", result.get("records").get(0).get("service").asText());
        assertEquals("14040821134014", result.get("records").get(0).get("dateTime").asText());
        assertEquals("000003782173", result.get("records").get(0).get("customerId").asText());
        assertTrue(result.get("records").get(0).get("accountIban").asText().startsWith("IR"));
    }
}
