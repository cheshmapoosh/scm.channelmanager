package ir.daneshrefah.scm.common.model.definition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.dto.definition.DefinitionRequest;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefinitionRequestDeserializer extends JsonDeserializer<DefinitionRequest> {

    @Override
    public DefinitionRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode root = codec.readTree(p);

        DefinitionRequest req = new DefinitionRequest();

        if (root.get("id") != null) {
            req.setId(root.get("id").asText());
        }
        if (root.get("name") != null) {
            req.setName(root.get("name").asText());
        }
        if (root.get("title") != null) {
            req.setTitle(root.get("title").asText());
        }
        if (root.get("engine") != null) {
            req.setEngine(TemplateEngineType.valueOf(root.get("engine").asText()));
        }
        if (root.get("type") != null) {
            req.setType(DefinitionType.valueOf(root.get("type").asText()));
        }
        JsonNode detailsNode = root.get("detail");
        if (detailsNode != null && detailsNode.isArray()) {
            List<DefinitionDetail> details = new ArrayList<>();
            Class<? extends DefinitionDetail> targetClass;
            switch (req.getType()) {
                case PLUGIN:
                    targetClass = PluginDefinitionDetail.class;
                    break;
                case JAVA:
                    targetClass = JavaDefinitionDetail.class;
                    break;
                default:
                    targetClass = DefinitionDetail.class;
            }
            for (JsonNode detailNode : detailsNode) {
                details.add(codec.treeToValue(detailNode, targetClass));
            }
            req.setDetail(details);
        }

        return req;
    }
}