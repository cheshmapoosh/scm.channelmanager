package ir.daneshrefah.scm.plugin.nab.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-22
 */
@RequiredArgsConstructor
//@Service
public class NabTcpRequestTransformer extends AbstractTransformer<Object> {

//    private final ObjectMapper objectMapper;

    @Override
    public Object internalTransform(Object payload, Message message, JsonNode metadata) {
        NabMessageHeader header = NabMessageHeader.builder()
                .serviceCode("")
                .rqUID("")
                .userId("")
                .password("")
                .build();
        NabRequestMessage requestMessage = new NabRequestMessage(header);
        return null;
    }

}
