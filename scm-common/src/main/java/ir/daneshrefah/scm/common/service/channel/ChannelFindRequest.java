package ir.daneshrefah.scm.common.service.channel;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import ir.daneshrefah.scm.common.model.terminal.ChannelProtocol;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-24
 */
@Data
public class ChannelFindRequest extends PagedRequestData {

    private String code;
    private String terminalCode;
    private ChannelProtocol protocol;

}
