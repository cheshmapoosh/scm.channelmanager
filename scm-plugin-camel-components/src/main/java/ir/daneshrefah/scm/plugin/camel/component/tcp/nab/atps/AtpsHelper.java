package ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ir.daneshrefah.scm.plugin.camel.config.NabProperties;
import ir.daneshrefah.scm.utils.calendar.shamsi.impl.ShamsiDateTime;
import ir.daneshrefah.scm.utils.date.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
@Component
public class AtpsHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static  NabProperties nabProperties;



    public AtpsHelper(NabProperties nabProperties) {
        AtpsHelper.nabProperties = nabProperties;
    }

    /**
     * Fixes string length by trimming or padding right with spaces.
     * @param value  Input value (nullable)
     * @param length Desired fixed length
     * @return String of exact length (padded or trimmed)
     */
    public static String fix(Object value, int length) {
        if (value == null) {
            return String.format("%-" + length + "s", "");
        }

        String str = value.toString().trim();
        if (str.length() > length) {
            str = str.substring(0, length);
        }
        return String.format("%-" + length + "s", str);
    }

    /**
     * Parses JSON string into a Map.
     * @param jsonString JSON input (String)
     * @return Map representation of JSON
     */
    public static Map<String, Object> toMap(String jsonString) {
        try {
            return mapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON input: " + jsonString, e);
        }
    }

    /**
     * Converts JSON string to fixed-length plaintext record.
     * Field lengths: command=2, service=2, dateTime=14,
     * cmUserId=10, cmPassword=10, rquid=16 (sum=58)
     */
    public static String jsonToPlainText(String jsonString) {
        Map<String, Object> json = toMap(jsonString);

        String command    = fix(json.get("command"), 2);
        String service    = fix(json.get("service"), 2);
        String dateTime   = fix(json.get("dateTime"), 14);
        String cmUserId   = fix(json.get("cmUserId"), 10);
        String cmPassword = fix(json.get("cmPassword"), 10);
        String rquid      = fix(json.get("rquid"), 16);

        return command + service + dateTime + cmUserId + cmPassword + rquid;
    }

    public static AtpsResponseHeader parseHeader(String response) {
        if (response == null || response.length() < 39) {
            throw new IllegalArgumentException("Response too short for ATPS header: " + response);
        }

        int idx = 0;
        String actionCode  = response.substring(idx, idx + 5); idx += 5;
        String command     = response.substring(idx, idx + 2); idx += 2;
        String service     = response.substring(idx, idx + 2); idx += 2;
        String dateTime    = response.substring(idx, idx + 14); idx += 14;
        String referenceNo = response.substring(idx, idx + 16); idx += 16;

        return new AtpsResponseHeader.AtpsResponseHeaderBuilder()
                .actionCode(actionCode.trim())
                .command(command.trim())
                .service(service.trim())
                .dateTime(dateTime.trim())
                .referenceNo(referenceNo.trim())
                .build();
    }

    public static ObjectNode parseFixedWidthResponse(String response) {
        if (response == null || response.length() < 61) {
            throw new IllegalArgumentException("Response too short: " + response);
        }

        // --- Header ---
        AtpsResponseHeader header = parseHeader(response);

        int idx = 39; // header size fixed
        String paymentCode        = response.substring(idx, idx + 6); idx += 6;
        String paymentDescription = response.substring(idx, idx + 16); idx += 16;

        // --- JSON mapping ---
        ObjectNode node = mapper.createObjectNode();
        node.put("actionCode", header.getActionCode());
        node.put("command", header.getCommand());
        node.put("service", header.getService());
        node.put("dateTime", header.getDateTime());
        node.put("referenceNo", header.getReferenceNo());

        node.put("paymentCode", paymentCode.trim());
        node.put("paymentDescription", paymentDescription.trim());


        if (response.length() > idx) {
//            throw new RuntimeException("Unexpected extra data in response: " + response.substring(idx));
        }

        return node;
    }


    public static ObjectNode[] parseFixedWidthArray(String[] records) {
        if (records == null) {
            return new ObjectNode[0];
        }

        ObjectNode[] nodes = new ObjectNode[records.length];
        for (int i = 0; i < records.length; i++) {

            String record = records[i] != null ? String.format("%-61s", records[i]) : "                                                               ";
            nodes[i] = parseFixedWidthResponse(record);
        }

        return nodes;
    }

    public static String enrichRequestBody(String command) {
        StringBuilder sb = new StringBuilder();
        sb.append(fix(command, 2));
        sb.append(fix("05", 2));
        sb.append(fix(getNowAsPersianDateTime(), 14));
        sb.append(fix(nabProperties.getUsername(), 10));
        sb.append(fix(nabProperties.getPassword(), 10));
        sb.append(fix("123456987", 16));
        return sb.toString();
    }
    private static String getNowAsPersianDateTime() {
        ShamsiDateTime currentDateTime = DateUtils.ShamsiCalendarConvertor.getCurrentDateTime();
        return currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String toString(Object inBody, Charset charset) {
        if (inBody == null) return "";
        if (inBody instanceof String s) {
            return s.trim();
        } else if (inBody instanceof byte[] bytes) {
            return new String(bytes, charset);
        } else if (inBody instanceof ByteBuf byteBuf) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            return new String(bytes, charset);
        } else {
            throw new IllegalArgumentException("Body must be String, byte[] or ByteBuf");

        }
    }

    public static ByteBuf toByteBuf(Object inBody, Charset charset) {
        if (inBody == null) {
            throw new IllegalArgumentException("Body must not be null");
        } else if (inBody instanceof ByteBuf byteBuf) {
            return byteBuf;
        } else if (inBody instanceof byte[] bytes) {
            return Unpooled.wrappedBuffer(bytes);
        } else if (inBody instanceof String s) {
            return Unpooled.wrappedBuffer(s.getBytes(charset));
        } else {
            throw new IllegalArgumentException("ATPS body must be byte[], String or ByteBuf");

        }
    }
}
