package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private PersonType personType;
    @NotNull
    private Nationality nationality;
    @NotNull
    @NotBlank //TODO Create NationalId Jakarta bean validation annotation
    private String nationalId;
    @NotBlankIfPresent
    private String subOrganizationId;

}
