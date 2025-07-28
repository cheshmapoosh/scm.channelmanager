package ir.daneshrefah.scm.uaa.domain.client;

import ir.daneshrefah.scm.common.AbstractModel;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientAuthorizationGrantType extends AbstractModel<Long> {
    //TODO creator and edit type on DB is difference
    private Long id;
    private AuthorizationGrantType authorizationGrantType;
}
