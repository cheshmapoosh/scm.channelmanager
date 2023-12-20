package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public abstract class AbstractInboundChannelGenerator {

    protected Channel channel;
    protected List<TerminalServiceChannelAccess> channelAccesses;
    protected ServiceProducerTemplate producerTemplate;
    private List<Authority> authorities;
    protected TransformerService transformerService;
    private Map<String, List<TransformerExecutionWrapper>> requestTransformerMap = new HashMap<>();

    private AuthenticationClientTemplate authenticationClientTemplate;

    public final void initInbound(ServiceProducerTemplate producerTemplate,
                                  Channel channel, List<TerminalServiceChannelAccess> channelAccesses,
                                  List<Authority> authorities, TransformerService transformerService,
                                  AuthenticationClientTemplate authenticationClientTemplate) {
        this.producerTemplate = producerTemplate;
        this.channel = channel;
        this.channelAccesses = channelAccesses;
        this.authorities = authorities;
        this.transformerService = transformerService;
        initConfig();
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            String terminalId = channelAccess.getTerminalServiceAccess().getTerminal().getId();
            List<TransformerRelation> terminalServiceRequestTransformers = transformerService
                    .findAllTransformerRelationsBySourceAndType(channelAccess.getTerminalServiceAccess().getId(), TransformerRelationType.TERMINAL_SERVICE_REQUEST);
            List<TransformerExecutionWrapper> requestTransformers = terminalServiceRequestTransformers.stream().map(t -> new TransformerExecutionWrapper(t)).collect(Collectors.toList());
            requestTransformerMap.put(terminalId + "_" + channelAccess.getTerminalServiceAccess().getService().getId(), requestTransformers);
            if (!requestTransformerMap.containsKey(terminalId)) {
                terminalServiceRequestTransformers = transformerService
                        .findAllTransformerRelationsBySourceAndType(channelAccess.getTerminalServiceAccess().getTerminal().getId(),
                                TransformerRelationType.TERMINAL_REQUEST);
                requestTransformers = terminalServiceRequestTransformers.stream().map(t -> new TransformerExecutionWrapper(t)).collect(Collectors.toList());
                requestTransformerMap.put(terminalId, requestTransformers);
            }
            registerTerminalService(channelAccess);
        }
        this.authenticationClientTemplate = authenticationClientTemplate;
        finalizeConfig();
    }

    private boolean checkServiceCallAllowed(TerminalServiceChannelAccess service, Message message) {
        if (!checkServiceAuthenticationAllowed(service, message))
            return false;
        if (!isServiceSecondAuthenticationAllowed(service, message))
            return false;
        if (!checkAccountAuthorizationAllowed(service, message))
            return false;
        if (!checkServiceAccessAllowed(service, message))
            return false;
        return isServiceWithdrawAllowed(service, message);
    }

    private boolean isServiceSecondAuthenticationAllowed(TerminalServiceChannelAccess serviceAccess, Message message) {
        Terminal terminal = serviceAccess.getTerminalServiceAccess().getTerminal();
        Service service = serviceAccess.getTerminalServiceAccess().getService();
        if (!terminal.getSupportCheckSecondAuthentication()) {
            return true;
        }
        if (!service.getCheckAccessSecondAuthentication()) {
            return true;
        }
//        TODO
//        3) if second authentication matched return true
        return true;
    }

    private boolean checkServiceAccessAllowed(TerminalServiceChannelAccess serviceAccess, Message message) {
        Terminal terminal = serviceAccess.getTerminalServiceAccess().getTerminal();
        Service service = serviceAccess.getTerminalServiceAccess().getService();
        if (!terminal.getSupportCheckServiceAccess()) {
            return true;
        }
        if (!service.getCheckAccessService()) {
            return true;
        }
//        TODO
//        3) check AccountAccessAuthority of user
        return true;
    }

    private boolean checkServiceAuthenticationAllowed(TerminalServiceChannelAccess serviceAccess, Message message) {
        Terminal terminal = serviceAccess.getTerminalServiceAccess().getTerminal();
        Service service = serviceAccess.getTerminalServiceAccess().getService();
        if (!terminal.getSupportCheckAuthentication()) {
            return true;
        }
        if (!service.getCheckAccessFirstAuthentication()) {
            return true;
        }

        //TODO uaa check authority
//        AuthenticationResponse authenticationResponse = (AuthenticationResponse) SecurityContextHolder.getContext().getAuthentication();
//        AuthorizationDecision decision = AuthorityAuthorizationManager.hasAnyAuthority("").check(() -> authenticationResponse, null);
//        if (decision != null && !decision.isGranted()) {
//            throw new AccessDeniedException("Access Denied");
//        }
//        TODO
//        3) if authentication matched return true
        return true;
    }

    private boolean checkAccountAuthorizationAllowed(TerminalServiceChannelAccess serviceAccess, Message message) {
        Terminal terminal = serviceAccess.getTerminalServiceAccess().getTerminal();
        Service service = serviceAccess.getTerminalServiceAccess().getService();
        if (!terminal.getSupportCheckAssetAccess()) {
            return true;
        }
        if (!service.getCheckAccessAsset()) {
            return true;
        }
//        TODO
//        3) check AccountAccessAuthority of user
        return true;
    }

    private boolean isServiceWithdrawAllowed(TerminalServiceChannelAccess service, Message message) {
//        for (Iterator<TerminalServiceWithdrawAuthority> iterator = serviceLimitations.iterator(); iterator.hasNext(); ) {
//            TerminalServiceWithdrawAuthority serviceLimitation = iterator.next();
//            if (!serviceLimitation.isGranted(service, message))
//                return false;
//        }
        return true;
    }

    protected abstract void finalizeConfig();

    protected abstract void initConfig();

    protected abstract void registerTerminalService(TerminalServiceChannelAccess channelAccess);

    protected final Message invokeService(TerminalServiceChannelAccess service, Message message) {
        if (!checkServiceCallAllowed(service, message)) {
            return message;
        }
        List<TransformerExecutionWrapper> transformerRelations = extractRequestTransformerList(service);
        JsonNode payload = message.getPayload();
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = (JsonNode) transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        message.setPayload(payload);
        producerTemplate.callService(service.getTerminalServiceAccess().getService(), message);
        return message;
    }

    private List<TransformerExecutionWrapper> extractRequestTransformerList(TerminalServiceChannelAccess service) {
        String terminalId = service.getTerminalServiceAccess().getTerminal().getId();
        String serviceId = service.getTerminalServiceAccess().getService().getId();
        List<TransformerExecutionWrapper> result = new ArrayList<>(requestTransformerMap.get(terminalId));
        result.addAll(requestTransformerMap.get(terminalId + "_" + serviceId));
        return result;
    }

}
