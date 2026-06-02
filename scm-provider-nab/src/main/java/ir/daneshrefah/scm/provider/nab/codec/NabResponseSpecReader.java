package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabResponseSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabResponseStatusSpec;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NabResponseSpecReader {
    private final JsonFieldSpecReader fieldSpecReader;

    public NabResponseSpec read(JsonNode root) {
        JsonNode responseNode = root == null ? null : root.get("response");
        if (responseNode == null || responseNode.isNull() || !responseNode.isObject()) {
            throw new IllegalArgumentException("NAB request must define response object");
        }
        NabResponseStatusSpec statusSpec = readStatus(responseNode.get("status"));
        String separator = JsonNodeSupport.text(responseNode, "recordSeparator");
        List<NabFieldSpec> fields = fieldSpecReader.readFields(responseNode.get("fields"), "response");
        return new NabResponseSpec(statusSpec, separator == null ? "\n" : separator, fields);
    }

    private NabResponseStatusSpec readStatus(JsonNode statusNode) {
        JsonNode fieldNode = statusNode == null ? null : statusNode.get("field");
        NabFieldSpec statusField;
        if (fieldNode == null || fieldNode.isNull() || fieldNode.isMissingNode()) {
            statusField = fieldSpecReader.readField(defaultStatusField(), "response.status");
        } else {
            statusField = fieldSpecReader.readField(fieldNode, "response.status");
        }
        String successCode = StringUtils.defaultIfBlank(JsonNodeSupport.text(statusNode, "successCode"), "00000");
        String successListCode = StringUtils.defaultIfBlank(JsonNodeSupport.text(statusNode, "successListCode"), "10000");
        return new NabResponseStatusSpec(statusField, successCode, successListCode);
    }

    private JsonNode defaultStatusField() {
        return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode()
                .put("name", "actionCode")
                .put("length", 5)
                .put("type", "STRING");
    }
}
