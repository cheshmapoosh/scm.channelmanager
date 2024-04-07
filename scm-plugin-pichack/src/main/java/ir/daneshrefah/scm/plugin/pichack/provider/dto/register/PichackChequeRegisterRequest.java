package ir.daneshrefah.scm.plugin.pichack.provider.dto.register;

import ir.daneshrefah.scm.plugin.pichack.provider.dto.common.*;
import lombok.Data;

import java.util.Collection;
import java.util.Date;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@Data
public class PichackChequeRegisterRequest extends BasePichackRequest {

    private Collection<BenefactorPerson> accountOwners;
    private Collection<BenefactorPerson> receivers;
    private Collection<ChequeSigner> signers;
    private String serialNo;
    private String seriesNo;
    private String fromIban;
    private Long amount;
    private Date dueDate;
    private String dueDateStr;
    private String description;
    private String toIban;
    private String branchCode;
    private ChequeType chequeType;
    private ChequeMedia chequeMedia;
    private String reason;

}
