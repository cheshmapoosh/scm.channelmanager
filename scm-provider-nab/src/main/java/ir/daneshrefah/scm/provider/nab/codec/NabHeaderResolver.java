package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.domain.NabCommandSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabHeaderValues;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NabHeaderResolver {
    private final NabRqUidGenerator rqUidGenerator;
    private final PersianDateFormatter dateFormatter;

    public NabHeaderValues resolve(JsonNode root, NabResolvedConfig config, NabCommandSpec commandSpec) {
        JsonNode header = root == null ? null : root.get("header");
        JsonNode data = root == null ? null : root.get("data");

        String terminalType = firstText(header, data, root, "terminalType");
        String channelCode = firstText(header, data, root, "channelCode");
        String serviceCode = firstNonBlank(
                text(header, "serviceCode"),
                config.serviceCodesByTerminalType().get(terminalType),
                config.serviceCodesByChannelCode().get(channelCode),
                config.defaultServiceCode()
        );
        String userId = firstNonBlank(text(header, "userId"), config.userId());
        String password = firstNonBlank(text(header, "password"), config.password());
        String rqUid = firstNonBlank(text(header, "rqUid"), text(header, "rqUID"), rqUidGenerator.generate(config));
        String clientAddress = firstText(header, data, root, "clientAddress");
        String dateTime = firstNonBlank(text(header, "dateTime"), dateFormatter.nowTimestamp());

        require("serviceCode", serviceCode);
        require("userId", userId);
        require("password", password);
        require("rqUid", rqUid);

        ObjectNode headerData = JsonNodeFactory.instance.objectNode();
        headerData.put("nabProtocol", commandSpec.protocol().name());
        headerData.put("protocol", commandSpec.protocol().name());
        headerData.put("clientAddress", clientAddress);
        headerData.put("command", commandSpec.code());
        headerData.put("serviceCode", serviceCode);
        headerData.put("dateTime", dateTime);
        headerData.put("userId", userId);
        headerData.put("password", password);
        headerData.put("rqUid", rqUid);
        return new NabHeaderValues(commandSpec.protocol().name(), commandSpec.code(), rqUid, headerData);
    }

    private String firstText(JsonNode header, JsonNode data, JsonNode root, String fieldName) {
        return firstNonBlank(text(header, fieldName), text(data, fieldName), text(root, fieldName));
    }

    private String text(JsonNode node, String fieldName) {
        return StringUtils.trimToNull(JsonNodeSupport.text(node, fieldName));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private void require(String name, String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("NAB header " + name + " is required");
        }
    }
}
