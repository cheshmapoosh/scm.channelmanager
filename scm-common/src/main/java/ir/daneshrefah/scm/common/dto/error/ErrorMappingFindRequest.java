package ir.daneshrefah.scm.common.dto.error;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMappingFindRequest extends PagedRequestData {
    private String providerId;
    private String providerErrorCode;
    private String errorMessage;
    private Integer scmErrorCode;
    private String exceptionOverrideName;
    private MessageStatus status;
    private Boolean bundleKey;

}
