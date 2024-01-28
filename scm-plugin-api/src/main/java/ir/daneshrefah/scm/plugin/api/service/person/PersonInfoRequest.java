package ir.daneshrefah.scm.plugin.api.service.person;

import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
@Getter
@Setter
public class PersonInfoRequest {

    private PersonType personType;
    private String nationalId;
    private String subOrganizationId;
    private Nationality nationality;

}
