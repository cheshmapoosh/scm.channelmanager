package ir.daneshrefah.scm.common.dto.channel;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
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
    private String creator;
    private String lastEditor;
    private String title;

}
