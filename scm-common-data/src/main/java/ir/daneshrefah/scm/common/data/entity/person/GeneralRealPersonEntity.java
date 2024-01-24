package ir.daneshrefah.scm.common.data.entity.person;

import ir.daneshrefah.scm.common.data.converter.GenderConverter;
import ir.daneshrefah.scm.common.data.converter.MaritalStatusConverter;
import ir.daneshrefah.scm.common.data.type.Gender;
import ir.daneshrefah.scm.common.data.type.MaritalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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



}
