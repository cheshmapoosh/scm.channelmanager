package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Data
public class Customer extends BaseModel<Long> {

    private Long id;
    private String customerNo;
//    private ExternalServiceProvider provider;

}
