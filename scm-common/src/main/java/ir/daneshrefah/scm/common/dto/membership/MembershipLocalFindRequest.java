package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
//import ir.daneshrefah.scm.common.model.customer.AssetType;
import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-02
 */
@Setter
@Getter
public class MembershipLocalFindRequest extends PagedRequestData {

    private PersonType personType;
    private String nationalId;
    private String subOrganizationId;
//    private AssetType assetType;

}
