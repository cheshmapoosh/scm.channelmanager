package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldType;
import ir.daneshrefah.scm.provider.nab.domain.NabOverflowPolicy;
import ir.daneshrefah.scm.provider.nab.domain.NabPadding;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JsonFieldSpecReader {

    public List<NabFieldSpec> readFields(JsonNode fieldsNode, String owner) {
        if (fieldsNode == null || fieldsNode.isNull() || fieldsNode.isMissingNode()) {
            return List.of();
        }
        if (!fieldsNode.isArray()) {
            throw new IllegalArgumentException(owner + ".fields must be an array");
        }
        List<NabFieldSpec> fields = new ArrayList<>();
        for (JsonNode fieldNode : fieldsNode) {
            fields.add(readField(fieldNode, owner));
        }
        return List.copyOf(fields);
    }

    public NabFieldSpec readField(JsonNode fieldNode, String owner) {
        if (fieldNode == null || !fieldNode.isObject()) {
            throw new IllegalArgumentException(owner + " field spec must be an object");
        }
        String name = StringUtils.trimToNull(JsonNodeSupport.text(fieldNode, "name"));
        if (name == null) {
            throw new IllegalArgumentException(owner + " field spec must define name");
        }
        JsonNode lengthNode = fieldNode.get("length");
        if (lengthNode == null || !lengthNode.canConvertToInt() || lengthNode.asInt() < 1) {
            throw new IllegalArgumentException(owner + " field " + name + " must define positive length");
        }
        String path = StringUtils.defaultIfBlank(JsonNodeSupport.text(fieldNode, "path"), JsonNodeSupport.pointerForName(name));
        String converter = StringUtils.defaultIfBlank(JsonNodeSupport.text(fieldNode, "converter"), "NONE");
        boolean required = fieldNode.has("required") && fieldNode.get("required").asBoolean(false);
        boolean trim = !fieldNode.has("trim") || fieldNode.get("trim").asBoolean(true);

        return new NabFieldSpec(
                name,
                path,
                lengthNode.asInt(),
                NabFieldType.from(JsonNodeSupport.text(fieldNode, "type")),
                required,
                converter,
                NabPadding.from(JsonNodeSupport.text(fieldNode, "padding")),
                NabOverflowPolicy.from(JsonNodeSupport.text(fieldNode, "overflow")),
                trim
        );
    }
}
