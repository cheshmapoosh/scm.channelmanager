package ir.daneshrefah.scm.core.integration.service.guard;

import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class IncomingChannelCodeResolver {
    public static final String SCM_CHANNEL_HEADER = "X-SCM-Channel";
    public static final String CHANNEL_CODE_HEADER = "channelCode";

    public Optional<String> resolve(Exchange exchange) {
        return firstNonBlank(
                exchange.getProperty(Message.CHANNEL_CODE, String.class),
                exchange.getMessage().getHeader(Message.CHANNEL_CODE, String.class),
                exchange.getMessage().getHeader(SCM_CHANNEL_HEADER, String.class),
                exchange.getMessage().getHeader(CHANNEL_CODE_HEADER, String.class));
    }

    private Optional<String> firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = normalize(value);
            if (normalized != null) {
                return Optional.of(normalized);
            }
        }
        return Optional.empty();
    }

    public String normalize(String value) {
        String normalized = StringUtils.trimToNull(value);
        return normalized != null ? normalized.toLowerCase(Locale.ROOT) : null;
    }
}
