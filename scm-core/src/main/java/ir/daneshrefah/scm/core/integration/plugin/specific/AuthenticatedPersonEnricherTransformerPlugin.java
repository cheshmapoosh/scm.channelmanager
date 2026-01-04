package ir.daneshrefah.scm.core.integration.plugin.specific;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Objects;

@Component("AuthenticatedPersonEnricherTransformerPlugin")
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedPersonEnricherTransformerPlugin implements PluginHandler {

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORMER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        PluginPhase phase = pluginDetail.getPhase();
        if (!PluginPhase.BEFORE.equals(phase)) {
            throw new IllegalArgumentException("Plugin phase " + phase + " is not supported on 'authenticatedPersonEnricherTransformerPlugin'");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        var loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        var person = Objects.requireNonNull(loggedInUser).getPerson();
        ValidationUtils.checkNull(person, () -> new NoMatchRecordFoundException("nationalId"));
        log.info("AuthenticatedPerson set into header of exchange!");
        exchange.getIn().setHeader("person", person);
    }
}
