package ir.daneshrefah.scm.uaa.service.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "client numeric instance id")
    private Long id;
    private String title;
    private String nickname;
    private String terminalCode;

}
