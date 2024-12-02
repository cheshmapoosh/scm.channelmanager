package ir.daneshrefah.scm.common.dto.rest;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExternalProviderResponse {
    private String id;
    private String code;
    private String title;

}
