package ir.daneshrefah.scm.common.dto.bundle;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BundleCreateRequest implements RequestData {
    @NotBlank
    private String locale;
    @NotBlank
    private String key;
    @NotBlank
    private String value;
}
