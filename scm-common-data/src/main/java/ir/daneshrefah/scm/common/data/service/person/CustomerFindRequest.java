package ir.daneshrefah.scm.common.data.service.person;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-06
 */
@Data
public class CustomerFindRequest implements RequestData {

    private PersonType personType;
    private Nationality nationality;
    private String nationalId;
    private String subOrganizationId;

}
