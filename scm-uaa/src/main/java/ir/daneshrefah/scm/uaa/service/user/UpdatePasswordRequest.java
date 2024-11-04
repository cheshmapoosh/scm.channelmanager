package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@Data
public class UpdatePasswordRequest implements RequestData {

    private String password;
    private String passwordConfirm;

}
