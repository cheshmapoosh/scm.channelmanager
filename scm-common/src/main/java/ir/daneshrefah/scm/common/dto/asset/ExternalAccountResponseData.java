package ir.daneshrefah.scm.common.dto.asset;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalAccountResponseData {
    private Long accountOwnerCustomerNo;
    private String accountOwnerName;
    private String ownerNationalId;
    private Integer accountOwnerCustomerTypeCode;
    private String accountOwnerCustomerTypeTitle;
    private Integer generalCode;
    private Integer subsidryCode;
    private String subsidryTitle;
    private Integer accountTypeCode;
    private String accountTypeTitle;
    private Long accountNumber;
    private Integer accountStatusCode;
    private String accountStatusTitle;
    private Integer branchCode;
    private String branchTitle;
    private Long accountOpenDate;
    private Integer accountIsCommercial;
    private String currency;
    private String iban;
    private Long sayahCode;
    private Integer customerRelationTypeCode;
    private String customerRelationTypeTitle;
    private Integer customerIsSigner;
    private Double customerSharePercent;
    private Long balanceTotal;
    private Long balanceAvailable;
    private Long blockTotal;
    private Long signDate;
    private Long lastTransactionDate;
    private String subOrganization;
    private String nickName;
    private Boolean favorite;
}
