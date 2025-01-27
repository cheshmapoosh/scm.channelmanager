package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Getter
@Setter
public class Account extends BaseModel<Long> {

    private String accountNo;
    private AccountType accountType;
    private AssetProvider assetProvider;
    private Integer close;
    private LocalDateTime closeDate;
    private Integer reasonClose;

}
