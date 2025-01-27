package ir.daneshrefah.scm.common.data.entity.person;

import jakarta.persistence.Column;
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
public abstract class GeneralLegalPersonEntity extends GeneralPersonEntity {

    @Column(name = "FIRST_NAME")
    private String title;
    @Column(name = "FIRST_NAME_ENGLISH")
    private String titleEnglish;
    @Column(name = "NATIONAL_CODE")
    private String nationalId;
    @Column(name = "SUB_ORGANIZATION_ID")
    private String subOrganizationId;
    @Column(name = "BIRTH_DATE")
    private LocalDate registerDate;

}
