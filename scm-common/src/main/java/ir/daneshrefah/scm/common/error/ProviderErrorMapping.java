package ir.daneshrefah.scm.common.error;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProviderErrorMapping {

    private String serviceProviderCode;
    private String serviceCode;
    private List<String> providerErrorCode;
    private List<String> providerErrorMessage;
}
