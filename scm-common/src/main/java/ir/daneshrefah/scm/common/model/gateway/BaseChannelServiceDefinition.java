package ir.daneshrefah.scm.common.model.gateway;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class BaseChannelServiceDefinition extends ChannelServiceDefinition {
    private Boolean checkLoginAuthentication;
    private AuthorizationConfig authorizationConfig;

    @Data
    public static class AuthorizationConfig {
        private String chain;
        private List<String> accessRoles;
    }

}
