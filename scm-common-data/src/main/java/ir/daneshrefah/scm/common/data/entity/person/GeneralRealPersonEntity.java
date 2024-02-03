package ir.daneshrefah.scm.common.data.entity.person;

import ir.daneshrefah.scm.common.data.converter.GenderConverter;
import ir.daneshrefah.scm.common.data.converter.MaritalStatusConverter;
import ir.daneshrefah.scm.common.data.type.Gender;
import ir.daneshrefah.scm.common.data.type.MaritalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Getter
@Setter
@Entity
public abstract class GeneralRealPersonEntity extends GeneralPersonEntity {

    @Column(name = "FIRST_NAME")
    private String firstName;
    private String firstNameEnglish;
    private String lastName;
    private String lastNameEnglish;
    private String fatherName;
    @Column(name = "GENDER_ID")
    @Convert(converter = GenderConverter.class)
    private Gender gender;
    @Convert(converter = MaritalStatusConverter.class)
    private MaritalStatus maritalStatus;
    @Column(name = "NATIONAL_CODE")
    private String nationalCode;
    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;
    @Transient
    private LocalDate deadDate;
    @Column(name = "ISSUE_DATE")
    private LocalDate registerIssueDate;
    @Column(name = "IDENTIFICATION_NO")
    private String identificationNo;
    @Column(name = "IDENTIFICATION_SERIAL")
    private String registerSeries;
    @Column(name = "IDENTIFICATION_SERIAL_NO")
    private String registerSerial;
    private String jobCode;
    @Transient
    private String jobTitle;
    private String educationCode;
    @Transient
    private String educationTitle;
    @Column(name = "REGION_CODE")
    private String stateCode;
    @Transient
    private String stateTitle;
    private String cityCode;
    @Transient
    private String cityTitle;
    @Transient
    private String shahabCode;
//    private String courseCode;
//    private String courseTitle;
//    private String documentTypeCode;
//    private String documentTypeTitle;
//    private boolean isLived;





//TODO    private String CUSTOMER_TYPE_CODE => REF.CUSTOMER_TYPE_CODE.CODE
//TODO    private String IDENTITY_DOCUMENT_TYPE
//TODO    private String EDUCATION_CODE => REF.EDUCATION.CODE = > diplom, lisans, ...
//TODO    private String MAJOR_CODE => REF.MAJOR
//TODO    private String ISSUE_PLACE
//TODO    private String BIRTH_PLACE => String


//UnUsed    private String TITLE_CODE => Mr/Ms
//UnUsed    private String CARD_TYPE_CODE



}
