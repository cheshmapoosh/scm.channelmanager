package ir.daneshrefah.scm.common.data.entity.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

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
@DiscriminatorValue("1")
public class IndividualPersonEntity extends GeneralRealPersonEntity {


}
