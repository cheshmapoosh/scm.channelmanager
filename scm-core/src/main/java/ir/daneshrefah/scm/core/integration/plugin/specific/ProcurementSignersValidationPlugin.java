package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Objects;

@Component("procurementSignersValidation")
@Slf4j
public class ProcurementSignersValidationPlugin implements PluginHandler {

    private final static String DELETE = "2";

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        if (!PluginPhase.BEFORE.equals(pluginDetail.getPhase())) {
            throw new IllegalArgumentException("Unsupported plugin phase: " + pluginDetail.getPhase());
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        JsonNode body = exchange.getIn().getBody(JsonNode.class);
        if (body.isNull() ) {
            log.warn("{} request is null", exchange.getProperty(Message.OPERATION, String.class));
            return;
        }

        JsonNode insDel = body.get("insDel");
        if (insDel == null || insDel.isNull()){
            log.warn("field insDel is null");
            return;
        }

        if (isDeleteRequest(insDel)) {
            validateLegalPerson();
        }
    }

    private void validateLegalPerson() {
        GeneralPerson person = getCurrentPerson();

        if (!(person instanceof GeneralLegalPerson)) {
            log.warn("Only legal customers can remove procurement agent!");
            throw new InvalidParameterException();
        }
    }


    private boolean isDeleteRequest(JsonNode insDel) {
        return DELETE.equals(insDel.asText());
    }

    private GeneralPerson getCurrentPerson() {
        var loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        var person = Objects.requireNonNull(loggedInUser).getPerson();
        ValidationUtils.checkNull(person, () -> new NoMatchRecordFoundException("nationalId"));
        return person;
    }
}
