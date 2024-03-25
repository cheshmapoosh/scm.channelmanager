package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.customer.AssetType;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Data
public class Membership extends BaseModel<Long> {

    private Long customerAccountId;
    private String nickname;
    private Boolean defaultAccount;
    private GeneralPerson person;
    private Customer customer;
    private AssetType assetType;
    private Account account;

}
