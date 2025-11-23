package ir.daneshrefah.scm.common.dto.channel;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-24
 */
@Getter
@Setter
public class ChannelFindRequest extends PagedRequestData {
    private String code;
    private String name;
    private String title;
}
