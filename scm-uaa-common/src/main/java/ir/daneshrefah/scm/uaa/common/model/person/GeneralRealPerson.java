package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.type.Gender;
import ir.daneshrefah.scm.uaa.common.type.MaritalStatus;
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
public abstract class GeneralRealPerson extends GeneralPerson {

    private String firstName;
    private String firstNameEnglish;
    private String lastName;
    private String lastNameEnglish;
    private String fatherName;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private String nationalCode;
    private LocalDate birthDate;

}
