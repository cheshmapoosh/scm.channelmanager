package ir.daneshrefah.scm.core.integration.plugin.support;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DestinationIbanResolver {

    private final PluginMessageValueReader valueReader;

    public String resolve(Message message, List<String> sources) {
        return valueReader.findFirstValue(message, sources);
    }
}
