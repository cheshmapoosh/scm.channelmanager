package ir.daneshrefah.scm.common.model.person;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Setter
public abstract class GeneralLegalPerson extends GeneralPerson {

    private String title;
    private String titleEnglish;
    private String nationalId;
    private String subOrganizationId;
    private LocalDate registerDate;

}
