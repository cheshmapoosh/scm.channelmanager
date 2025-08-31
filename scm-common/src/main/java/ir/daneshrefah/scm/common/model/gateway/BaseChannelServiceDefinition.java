package ir.daneshrefah.scm.common.model.gateway;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class BaseChannelServiceDefinition extends ChannelServiceDefinition {
    private Boolean checkLoginAuthentication;
    private List<String> accessRoles;
}
