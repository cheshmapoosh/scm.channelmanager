package ir.daneshrefah.scm.common.dto.bundle;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validaton.bean.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BundleEditRequest implements RequestData {
    @Numeric
    private Long id;
    @NotBlank
    private String value;
    @NotNull
    private LocalDateTime lastEditDate;
}
