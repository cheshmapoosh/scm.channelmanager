package ir.daneshrefah.scm.uaa.repository.authentication;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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


}
