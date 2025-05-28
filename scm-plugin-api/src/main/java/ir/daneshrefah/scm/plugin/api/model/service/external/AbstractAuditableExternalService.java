package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.common.model.service.ScmService;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Getter
@Setter
public abstract class AbstractAuditableExternalService<T extends AbstractAuditableExternalServiceProvider> extends ScmService {

    private T serviceProvider;
    private ExternalServiceBodyType requestBodyType;

}
