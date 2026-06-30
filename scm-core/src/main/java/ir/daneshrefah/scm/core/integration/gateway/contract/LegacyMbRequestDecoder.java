package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component("legacyMbRequestDecoder")
public class LegacyMbRequestDecoder extends JsonScmRequestDecoder {
    public LegacyMbRequestDecoder(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
