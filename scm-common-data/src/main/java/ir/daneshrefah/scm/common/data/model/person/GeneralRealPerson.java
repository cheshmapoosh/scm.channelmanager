package ir.daneshrefah.scm.common.data.model.person;

import ir.daneshrefah.scm.common.data.type.Gender;
import ir.daneshrefah.scm.common.data.type.MaritalStatus;
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
    private LocalDate deadDate;
    private LocalDate registerIssueDate;
    private String identificationNo;
    private String registerSeries;
    private String registerSerial;
    private String jobCode;
    private String jobTitle;
    private String educationCode;
    private String educationTitle;
    private String stateCode;
    private String stateTitle;
    private String cityCode;
    private String cityTitle;
    private String branchCode;
    private String shahabCode;
    private String courseCode;
    private String courseTitle;
    private String documentTypeCode;
    private String documentTypeTitle;
    private boolean isLived;

}
