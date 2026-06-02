package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component("legacyMbCardRequestDecoder")
public class LegacyMbCardRequestDecoder extends JsonScmRequestDecoder {
    public LegacyMbCardRequestDecoder(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
