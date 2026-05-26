package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.nab.domain.NabCommandSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabProtocol;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class NabCommandSpecReader {

    public NabCommandSpec read(JsonNode root) {
        JsonNode commandNode = root == null ? null : root.get("command");
        if (commandNode == null || commandNode.isNull()) {
            throw new IllegalArgumentException("NAB request must define command");
        }
        if (commandNode.isTextual()) {
            throw new IllegalArgumentException("NAB command object must define code and protocol");
        }
        String code = StringUtils.trimToNull(JsonNodeSupport.text(commandNode, "code"));
        if (code == null) {
            throw new IllegalArgumentException("NAB command.code is required");
        }
        return new NabCommandSpec(code, NabProtocol.from(JsonNodeSupport.text(commandNode, "protocol")));
    }
}
