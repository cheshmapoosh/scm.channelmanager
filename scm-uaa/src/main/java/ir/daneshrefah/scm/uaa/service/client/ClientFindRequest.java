package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-20
 */
@Getter
@Setter
public class ClientFindRequest extends PagedRequestData {

    private Long id;
    private String title;
    private String clientId;
    private String terminalCode;

}
