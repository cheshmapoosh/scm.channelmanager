package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.provider.nab.domain.NabResponseSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabResponseStatusSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class NabResponseParser {
    private final ObjectMapper objectMapper;
    private final FixedLengthDecoder decoder;

    public ObjectNode parse(String response, NabResponseSpec responseSpec) {
        String safeResponse = response == null ? "" : response;
        NabResponseStatusSpec statusSpec = responseSpec.status();
        String firstLine = firstLine(safeResponse, responseSpec.recordSeparator());
        String code = statusCode(firstLine, statusSpec);

        ObjectNode result = objectMapper.createObjectNode();
        ObjectNode status = statusNode(code, statusSpec.successCode().equals(code), statusSpec.successListCode().equals(code));
        result.set("status", status);

        if (statusSpec.successCode().equals(code)) {
            String dataFragment = afterStatus(firstLine, statusSpec);
            result.set("data", decoder.decode(dataFragment, responseSpec.fields()));
            log.debug("Parsed successful NAB response code={} fields={}", code, responseSpec.fields().size());
            return result;
        }

        if (statusSpec.successListCode().equals(code)) {
            ArrayNode records = objectMapper.createArrayNode();
            Arrays.stream(safeResponse.split(java.util.regex.Pattern.quote(responseSpec.recordSeparator())))
                    .filter(line -> line != null && !line.isBlank())
                    .forEach(line -> parseRecord(line, responseSpec, records));
            result.set("records", records);
            log.debug("Parsed successful NAB list response code={} records={}", code, records.size());
            return result;
        }

        log.warn("NAB returned unsuccessful response actionCode={}", code);
        return result;
    }

    private void parseRecord(String line, NabResponseSpec responseSpec, ArrayNode records) {
        String recordCode = statusCode(line, responseSpec.status());
        if (!responseSpec.status().successListCode().equals(recordCode)) {
            log.warn("Skipping NAB list record with unexpected actionCode={}", recordCode);
            return;
        }
        records.add(decoder.decode(afterStatus(line, responseSpec.status()), responseSpec.fields()));
    }

    private ObjectNode statusNode(String code, boolean success, boolean list) {
        ObjectNode status = objectMapper.createObjectNode();
        status.put("code", code);
        status.put("success", success || list);
        status.put("list", list);
        return status;
    }

    private String statusCode(String line, NabResponseStatusSpec statusSpec) {
        String safeLine = line == null ? "" : line;
        int length = statusSpec.field().length();
        if (safeLine.length() < length) {
            return safeLine.trim();
        }
        return safeLine.substring(0, length).trim();
    }

    private String afterStatus(String line, NabResponseStatusSpec statusSpec) {
        String safeLine = line == null ? "" : line;
        int length = statusSpec.field().length();
        if (safeLine.length() <= length) {
            return "";
        }
        return safeLine.substring(length);
    }

    private String firstLine(String response, String separator) {
        if (response == null || response.isEmpty()) {
            return "";
        }
        int index = response.indexOf(separator);
        return index < 0 ? response : response.substring(0, index);
    }
}
