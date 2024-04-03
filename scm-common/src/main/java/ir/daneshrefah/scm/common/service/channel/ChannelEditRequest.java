package ir.daneshrefah.scm.common.service.channel;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.terminal.ChannelProtocol;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-03
 */
@Data
public class ChannelEditRequest implements RequestData {

    private String id;
    private String code;
    private String title;
    private String terminalCode;
    private ChannelProtocol protocol;
    private String channelClassName;
    private String metadata;
    private LocalDateTime lastEditDate;

}
