package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.constant.AssetProviderCode;
import ir.daneshrefah.scm.common.model.service.ScmService;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-22
 */
@Getter
@Setter
public class AssetProvider extends AbstractAuditableModel<Integer> {

    private String name;
    private AssetProviderCode code;
    private boolean active;
    private String abbreviation;
    private ScmService service;

}
