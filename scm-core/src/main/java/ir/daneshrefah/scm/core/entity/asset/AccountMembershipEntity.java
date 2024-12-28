package ir.daneshrefah.scm.core.entity.asset;

//import ir.daneshrefah.scm.common.model.customer.AssetType;
//import ir.daneshrefah.scm.common.model.customer.AssetTypeCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
//@DiscriminatorValue(AssetTypeCode.ACCOUNT)
public class AccountMembershipEntity extends MembershipEntity {

    @Column(name = "DEFAULT_ACCOUNT")
    private Boolean defaultAccount;
}
