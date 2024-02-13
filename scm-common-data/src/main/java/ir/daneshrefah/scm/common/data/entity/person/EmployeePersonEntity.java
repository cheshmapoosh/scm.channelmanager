package ir.daneshrefah.scm.common.data.entity.person;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Entity
@DiscriminatorValue("2")
public class EmployeePersonEntity extends GeneralRealPersonEntity {

    @Transient
    private String personnelNo;
    @Transient
    private String employeeBranchCode;

}
