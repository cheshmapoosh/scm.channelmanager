package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.uaa.common.type.MaritalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Getter
@Setter
public abstract class GeneralRealPersonEntity extends GeneralPersonEntity {

    private MaritalStatus maritalStatus;

}
