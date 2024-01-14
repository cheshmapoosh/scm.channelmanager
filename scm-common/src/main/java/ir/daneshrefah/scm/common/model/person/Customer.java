package ir.daneshrefah.scm.common.model.person;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
// REF.CUSTOMER
@Getter
@Setter
public class Customer extends BaseModel {

    private String providerId;
    private String customerNo;
    private List<? extends Asset> assets;

}
