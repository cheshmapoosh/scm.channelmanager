package ir.daneshrefah.scm.common.data.entity.person;

import ir.daneshrefah.scm.common.data.converter.GenderConverter;
import ir.daneshrefah.scm.common.model.person.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
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
public abstract class GeneralLegalPersonEntity extends GeneralPersonEntity {

    @Column(name = "FIRST_NAME")
    private String title;
    @Column(name = "FIRST_NAME_ENGLISH")
    private String titleEnglish;
    @Column(name = "NATIONAL_CODE")
    private String nationalId;
    @Column(name = "SUB_ORGANIZATION_ID")
    private String subOrganizationId;

    /* Mandatory fields for persistence */

    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "BIRTH_DATE")
    private LocalDateTime registerDate;
    @Column(name = "IDENTIFICATION_NO")
    private String identificationNo;
    @Column(name = "FATHER_NAME")
    private String fatherName;
    @Column(name = "IDENTIFICATION_SERIAL_NO")
    private String identificationSerial;
    @Column(name = "GENDER_ID")
    @Convert(converter = GenderConverter.class)
    private Gender gender;
}
