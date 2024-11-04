package ir.daneshrefah.scm.common.dto.channel;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.terminal.ChannelProtocol;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-03
 */
@Data
public class ChannelCreateRequest implements RequestData {

    private String code;
    private String title;
    private String terminalCode;
    private ChannelProtocol protocol;
    private String channelClassName;
    private JsonNode metadata;

}
