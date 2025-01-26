package ir.daneshrefah.scm.common.dto.error;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMappingCreateRequest implements RequestData {

    @NotBlankIfPresent
    private String providerId;
    @NotBlankIfPresent
    private String providerErrorCode;
    @NotBlank
    private String errorMessage;
    @Numeric
    private Integer scmErrorCode;
    @NotBlankIfPresent
    private String exceptionOverrideName;
    @NotNull
    private MessageStatus status;
    @NotNull
    private Boolean bundleKey;
}
