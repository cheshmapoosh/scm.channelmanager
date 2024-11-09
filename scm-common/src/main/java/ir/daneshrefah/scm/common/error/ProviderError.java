package ir.daneshrefah.scm.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProviderError {
    private String providerErrorCode;
    private String providerErrorMessage;
}
