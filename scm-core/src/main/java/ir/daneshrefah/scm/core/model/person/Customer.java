package ir.daneshrefah.scm.core.model.person;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;
import lombok.Getter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
// REF.CUSTOMER
@Getter
public class Customer extends BaseModel {

    private ExternalServiceProvider provider;
    private String customerNo;
    private List<Asset> assets;

}
