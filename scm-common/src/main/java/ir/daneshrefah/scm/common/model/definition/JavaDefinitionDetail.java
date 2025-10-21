package ir.daneshrefah.scm.common.model.definition;

import ir.daneshrefah.scm.common.model.gateway.BaseChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JavaDefinitionDetail extends DefinitionDetail {
    private String path;
    private HttpMethod method;
    private boolean checkLoginAuthentication;
    private BaseChannelServiceDefinition.AuthorizationConfig authorizationConfig;
}