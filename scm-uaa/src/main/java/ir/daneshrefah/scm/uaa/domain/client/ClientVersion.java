package ir.daneshrefah.scm.uaa.domain.client;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-31
 */
@Getter
@Setter
public class ClientVersion extends BaseModel<Long> {

    private String version;
    private boolean isForced;
    private String signature;
    private ClientVersionStatus status;
    private Long clientId;

}
