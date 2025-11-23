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
        /**
         * Name of chainManager bean. if using 'registerDynamically()' on chain manager, you must add 'authorities' list
         */
        private String chain;
        private List<String> accessRoles;
        /**
         * List of AuthorizationManager voter bean name.
         */
        private List<String> authorities;
    }

}
