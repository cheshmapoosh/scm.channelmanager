package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
@Getter
@Setter
public class ExternalServiceProvider extends BaseModel<String> {

    private String code;
    private String title;
    private String providerClassName;
    private String metadata;
    private boolean customerProvided;
    private String customerProviderClassName;

}
