package ir.daneshrefah.scm.uaa.repository.authentication;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Entity
@DiscriminatorValue("3")
public class CorporatePersonEntity extends GeneralPersonEntity {

    @Column(name = "FIRST_NAME")
    private String title;
    @Column(name = "NATIONAL_CODE")
    private String nationalId;
    private String subOrganizationId;
    @Column(name = "BIRTH_DATE")
    private LocalDate registerDate;

}
