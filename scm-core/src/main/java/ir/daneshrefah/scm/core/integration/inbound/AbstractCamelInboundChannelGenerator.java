package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.ResponseBuilder;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.CustomerDataProviderService;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationType;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-05
 */
public abstract class AbstractCamelInboundChannelGenerator extends AbstractInboundChannelGenerator<Exchange> {

    @Getter(AccessLevel.PROTECTED)
    private CamelContext context;

    protected AbstractCamelInboundChannelGenerator(ObjectMapper objectMapper, CamelContext context,
                                                   AuthenticationClientTemplate authenticationTemplate,
                                                   ServiceProducerTemplate producerTemplate,
                                                   TransformerService transformerService,
                                                   ResponseBuilder<Exchange> responseBuilder,
                                                   DecisionManager decisionManager,
                                                   CustomerDataProviderService customerService) {
        super(objectMapper, authenticationTemplate, producerTemplate, transformerService,
                responseBuilder, decisionManager, customerService);
        this.context = context;
    }

    @Override
    protected ClientAuthenticationRequest extractAuthenticationRequest(Exchange input) {
        String authorizationHeader = CamelUtils.getAuthorizationHeaderFromExchange(input);

        String username = CamelUtils.getUsernameHeaderFromExchange(input);
        String terminalCode = CamelUtils.getTerminalCodeFromExchange(input);
        String authenticationValue = extractAuthenticationValue(authorizationHeader);
        ClientAuthenticationType authenticationType = extractAuthenticationType(authorizationHeader);
        String transactionValue = CamelUtils.getClaimCodeFromExchange(input);
        ClientAuthenticationType transactionType = StringUtils.isEmpty(transactionValue) ?
                ClientAuthenticationType.ANONYMOUS : ClientAuthenticationType.BASIC;

        ClientAuthenticationRequest request = ClientAuthenticationRequest.builder()
                .username(username)
                .terminalCode(terminalCode)
                .authenticationType(authenticationType)
                .authenticationValue(authenticationValue)
                .transactionType(transactionType)
                .transactionValue(transactionValue)
                .build();

        return request;
    }

    private String extractAuthenticationValue(String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        String[] args = authorizationHeader.split(" ");
        if (args.length < 2)
            return null;
        return args[1];
    }

    private ClientAuthenticationType extractAuthenticationType(String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return ClientAuthenticationType.ANONYMOUS;
        }

        String AUTHENTICATION_SCHEME_BASIC = "Basic";
        String AUTHENTICATION_SCHEME_BEARER = "Bearer";
        String AUTHENTICATION_SCHEME_SESSION = "Session";

        if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BASIC)) {
            return ClientAuthenticationType.BASIC;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_SESSION)) {
            return ClientAuthenticationType.SESSION;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BEARER)) {
            return ClientAuthenticationType.BEARER;
        }
        return ClientAuthenticationType.ANONYMOUS;
    }
}
