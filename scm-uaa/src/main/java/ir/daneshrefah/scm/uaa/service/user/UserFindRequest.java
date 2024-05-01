package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@Data
public class UserFindRequest extends PagedRequestData {
    private String nickname;
    private String terminalCode;
    private Boolean active;
    private String creatorBranch;
    private Integer terminalId;
}
