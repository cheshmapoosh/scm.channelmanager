package ir.daneshrefah.scm.common.data.entity.person;

import ir.daneshrefah.scm.common.data.converter.GenderConverter;
import ir.daneshrefah.scm.common.data.converter.MaritalStatusConverter;
import ir.daneshrefah.scm.common.model.person.Gender;
import ir.daneshrefah.scm.common.model.person.MaritalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    @Column(name = "FIRST_NAME_ENGLISH")
    private String firstNameEnglish;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "LAST_NAME_ENGLISH")
    private String lastNameEnglish;
    @Column(name = "FATHER_NAME")
    private String fatherName;
    @Column(name = "NATIONAL_CODE")
    private String nationalCode;
    @Column(name = "IDENTIFICATION_NO")
    private String identificationNo;
    @Column(name = "IDENTIFICATION_SERIAL")
    private String identificationSeries;
    @Column(name = "IDENTIFICATION_SERIAL_NO")
    private String identificationSerial;
    /**
     * values are in REF.IDENTITY_DOCUMENT_TYPE table
     * from CIF comes from 'CUSTOMERDOC' and 'DOCTITLE'
     * */
    @Column(name = "IDENTITY_DOCUMENT_TYPE")
    private String identificationDocumentTypeCode;
    @Convert(converter = MaritalStatusConverter.class)
    private MaritalStatus maritalStatus;
    /**
     * values are in REF.CUSTOMERJOB table
     * from CIF comes from 'CUSTOMERJOB' and 'JOBTITLE'
     * */
    @Column(name = "JOB_CODE")
    private String jobCode;
    /**
     * values are in REF.EDUCATION table
     * from CIF comes from 'CUSTOMEREDUCATION' and 'EDUCATIONTITLE'
    * */
    @Column(name = "EDUCATION_CODE")
    private String educationCode;
    /**
     * values are in REF.MAJOR table
     * from CIF comes from 'CUSTOMERCOURSE' and 'COURSETITLE'
    * */
    @Column(name = "MAJOR_CODE")
    private String majorCode;
    @Column(name = "GENDER_ID")
    @Convert(converter = GenderConverter.class)
    private Gender gender;
    @Column(name = "BIRTH_DATE")
    private LocalDateTime birthDate;
    @Transient
    private LocalDateTime deadDate;
    @Transient
    private boolean isLived;
//    @Column(name = "REGION_CODE")
//    private String stateCode;
//    private String cityCode;

//TODO    private String CUSTOMER_TYPE_CODE => REF.CUSTOMER_TYPE_CODE.CODE
//TODO    private String ISSUE_PLACE
//TODO    private String BIRTH_PLACE => String


//UnUsed    private String TITLE_CODE => Mr/Ms
//UnUsed    private String CARD_TYPE_CODE



}
