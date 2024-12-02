package ir.daneshrefah.scm.common.dto.error;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorMappingEditRequest implements RequestData {
    @NotBlank
    private String id;
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
    private LocalDateTime lastEditDate;
    @NotNull
    private Boolean bundleKey;
}
