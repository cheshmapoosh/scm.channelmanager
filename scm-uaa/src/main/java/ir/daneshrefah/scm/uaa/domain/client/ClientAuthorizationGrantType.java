package ir.daneshrefah.scm.uaa.domain.client;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientAuthorizationGrantType extends BaseModel<Long> {
    private Long id;
    private AuthorizationGrantType authorizationGrantType;
}
