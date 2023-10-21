package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hazelcast.internal.util.StringUtil;
import ir.daneshrefah.scm.common.model.authentication.Authentication;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.uaa.client.service.UaaClientAuthenticationService;
import ir.daneshrefah.scm.uaa.common.model.Constants;
import ir.daneshrefah.scm.uaa.common.model.authentication.AuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.AuthenticationResponse;
import ir.daneshrefah.scm.uaa.common.model.authentication.AuthenticationType;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ir.daneshrefah.scm.plugin.api.constants.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-26
 */
public class RestMessageParser {
    private final UaaClientAuthenticationService uaaClientAuthenticationService;
    Map<String, BodyExtractor> bodyExtractorMap = new HashMap<>();

    public RestMessageParser(UaaClientAuthenticationService uaaClientAuthenticationService) {
        bodyExtractorMap.put(HTTP_HEADER_CONTENT_TYPE_JSON, RestMessageParser::bodyExtractorJson);
        this.uaaClientAuthenticationService = uaaClientAuthenticationService;
    }

    public Message extractBody(Exchange exchange, TerminalServiceChannelAccess channelAccess) {

        Header header = new Header();
        header.setService(channelAccess);
        header.setContentType(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CONTENT_TYPE, String.class));
        String token = exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_AUTHORIZATION, String.class);
        if (StringUtils.isNotEmpty(token)) {
            Authentication a = new Authentication(token.replace("Bearer ", ""));
            header.setAuthentication(a);
        }

//        TODO uaa client
        String client = exchange.getMessage().getHeader(Constants.CHANNEL_HEADER, String.class);

        String agent = exchange.getMessage().getHeader(Constants.AGENT_HEADER, String.class);

        String deviceModel = exchange.getMessage().getHeader(Constants.DEVICE_MODEL_HEADER, String.class);

        String appVersion = exchange.getMessage().getHeader(Constants.APP_VERSION_HEADER, String.class);

        String signature = exchange.getMessage().getHeader(Constants.SIGNATURE_HEADER, String.class);

        String accessParameter = exchange.getMessage().getHeader(Constants.ACCESS_PARAM_HEADER, String.class);

        String uuid = exchange.getMessage().getHeader(Constants.UUID_HEADER, String.class);

        String ip = exchange.getMessage().getHeader(Constants.IP_HEADER, String.class);

        String registryToken = exchange.getMessage().getHeader(Constants.REG_TOKEN_HEADER, String.class);

        Integer otp = exchange.getMessage().getHeader(Constants.OTP_HEADER, Integer.class);

        String authorizationToken = exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_AUTHORIZATION, String.class);
        String tokenKey = StringUtils.substringBefore(authorizationToken, StringUtils.SPACE);
        AuthenticationType authenticationType = AuthenticationType.find(tokenKey);

        String delegateAuthorizationToken = exchange.getMessage().getHeader(Constants.DELEGATED_AUTH_HEADER, String.class);
        String delegateTokenKey = StringUtils.substringBefore(delegateAuthorizationToken, StringUtils.SPACE);
        AuthenticationType delegateAuthenticationType = AuthenticationType.find(delegateTokenKey);

        //TODO set client authorization token after uaa client changed by samaneh


        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setClient(client);
        authenticationRequest.setAgent(agent);
        authenticationRequest.setDeviceModel(deviceModel);
        authenticationRequest.setClientVersion(appVersion);
        authenticationRequest.setSignature(signature);
        authenticationRequest.setAccessParameter(accessParameter);
        authenticationRequest.setUuid(uuid);
        authenticationRequest.setIp(ip);
        authenticationRequest.setRegistryToken(registryToken);
        authenticationRequest.setClaimCode(otp);
        authenticationRequest.setAuthorizationToken(authorizationToken);

        AuthenticationRequest delegatedUserAuthenticationRequest = null;
        if (delegateAuthenticationType != null) {
            delegatedUserAuthenticationRequest = new AuthenticationRequest();
            delegatedUserAuthenticationRequest.setClient(client);
            delegatedUserAuthenticationRequest.setAgent(agent);
            delegatedUserAuthenticationRequest.setDeviceModel(deviceModel);
            delegatedUserAuthenticationRequest.setClientVersion(appVersion);
            delegatedUserAuthenticationRequest.setSignature(signature);
            delegatedUserAuthenticationRequest.setAccessParameter(accessParameter);
            delegatedUserAuthenticationRequest.setUuid(uuid);
            delegatedUserAuthenticationRequest.setIp(ip);
            delegatedUserAuthenticationRequest.setRegistryToken(registryToken);
            delegatedUserAuthenticationRequest.setClaimCode(otp);
            delegatedUserAuthenticationRequest.setAuthorizationToken(authorizationToken);

        }


        boolean isContextSyncRequired = true;
        AuthenticationResponse authenticationResponse = (AuthenticationResponse) uaaClientAuthenticationService.authenticate(authenticationRequest, delegatedUserAuthenticationRequest, isContextSyncRequired);
        SecurityContextHolder.getContext().setAuthentication(authenticationResponse);

//        if (StringUtils.isNotEmpty(token)) {
//            String tokenDelegated = exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_AUTHORIZATION_DELEGATED, String.class);
//            Authentication authentication = AuthenticationVerifier.verifyAuthenticationRequest(token, tokenDelegated);
//            header.setAuthentication(authentication);
//        }
//        String claim = exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLAIM, String.class);
//        if (StringUtils.isNotEmpty(claim) /*&& service.checkSecondLevel()*/) {
//            boolean authentication = AuthenticationVerifier.verifySecondAuthenticationRequest(token);
//            header.setSecondLevelAuthenticated(authentication);
//        }
//        Authorization: Basic base64(username:password)
//        Authorization: Digest username="username", realm="realm", nonce="nonce", uri="uri", response="hash"
//        Authorization: Bearer token
//        Authorization: Session sessionKey

        header.setClientCorrelationId(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLIENT_CORRELATION_ID, String.class));
        header.setCorrelationId(StringUtils.generateGuid());
        header.setChannel(channelAccess.getChannel()); //HttpConstants.HTTP_HEADER_CHANNEL
        header.setTerminal(channelAccess.getTerminalServiceAccess().getTerminal()); //HttpConstants.HTTP_HEADER_TERMINAL
//        header.setClientTransactionTimestamp(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLIENT_TIMESTAMP, String.class));
        header.setAccessParameter(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_ACCESS_PARAMETER, String.class));
        header.setReceiveTimestamp(LocalDateTime.now());

        Message message = new Message();
        message.setHeader(header);
        message.setStatus(Status.SC_PROCESSING);

        String contentType = exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CONTENT_TYPE, String.class);
        BodyExtractor bodyExtractor = bodyExtractorMap.get(contentType);
        if (null == bodyExtractor) {
            bodyExtractor = RestMessageParser::bodyExtractorNull;
        }
        message = bodyExtractor.transform(exchange, message);
        List<String> pathVariables = extractPathVariables(channelAccess.getTerminalServiceAccess().getService().getAlias());
        for (Iterator<String> iterator = pathVariables.iterator(); iterator.hasNext(); ) {
            String pathVariable = iterator.next();
            String pathVariableValue = exchange.getMessage().getHeader(pathVariable, String.class);
            if (message.getPayload() instanceof NullNode) {
                message.setPayload(JsonNodeFactory.instance.objectNode());
            }
            ((ObjectNode) message.getPayload()).put(pathVariable, pathVariableValue);
        }

        return message;
    }

    public static List<String> extractPathVariables(String urlPattern) {
        List<String> pathVariables = new ArrayList<>();
        if (StringUtils.isEmpty(urlPattern))
            return pathVariables;

        // Define a regular expression pattern to match path variables in curly braces
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(urlPattern);

        // Find and add path variable names to the list
        while (matcher.find()) {
            pathVariables.add(matcher.group(1));
        }

        return pathVariables;
    }

    @FunctionalInterface
    interface BodyExtractor {
        Message transform(Exchange exchange, Message message);
    }

    // Define your transformation methods
    static Message bodyExtractorJson(Exchange exchange, Message message) {
        JsonNode requestBody = exchange.getMessage().getBody(JsonNode.class);

        message.setPayload(requestBody);

        return message;
    }

    static Message bodyExtractorNull(Exchange exchange, Message message) {
        JsonNode requestBody = NullNode.getInstance();

        message.setPayload(requestBody);

        return message;
    }

}