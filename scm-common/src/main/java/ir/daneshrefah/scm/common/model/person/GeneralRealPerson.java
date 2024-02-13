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
public abstract class GeneralRealPerson extends GeneralPerson {

    private String firstName;
    private String firstNameEnglish;
    private String lastName;
    private String lastNameEnglish;
    private String fatherName;
    private String nationalCode;
    private String identificationNo;
    private String identificationSeries;
    private String identificationSerial;
    private String identificationDocumentTypeCode;
    private String identificationDocumentTypeTitle;
    private MaritalStatus maritalStatus;
    private String maritalStatusTitle;
    private String jobCode;
    private String jobTitle;
    private String educationCode;
    private String educationTitle;
    private String majorCode;
    private String majorTitle;
    private Gender gender;
    private String genderTitle;
    private LocalDate birthDate;
    private LocalDate deadDate;
    private boolean isLived;

}
