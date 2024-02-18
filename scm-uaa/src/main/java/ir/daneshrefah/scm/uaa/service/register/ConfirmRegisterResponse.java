package ir.daneshrefah.scm.uaa.service.register;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-18
 */
@Builder
@Getter
public class ConfirmRegisterResponse implements Serializable {

    private final boolean isSuccessful;
    private final String activationCode;

}
