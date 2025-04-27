package ir.daneshrefah.scm.core.integration.template;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;

import java.util.Map;

public interface ServiceMetadataTemplate {
    public static final String FREEMARKER = "freemarker";
    public static final String PLACEHOLDER = "placeholder";

    String render(Message message);
    JsonNode renderAsJson(Message message);
    Map<String, Object> renderAsMap(Message message);


}
