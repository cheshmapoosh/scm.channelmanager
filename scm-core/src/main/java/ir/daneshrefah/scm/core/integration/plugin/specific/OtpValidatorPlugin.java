package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.otp.service.OtpClientService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("OtpValidatorPlugin")
@RequiredArgsConstructor
public class OtpValidatorPlugin implements PluginHandler {

    private final OtpClientService otpClientService;

    private static final String OTP_CODE = "otpCode";
    private static final String OTP_CODE_HEADER = "X-Scm-Claim";
    private static final String OTP_REASON = "otpReason";
    private static final String AUTHORIZATION = "Authorization";

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
        String bodyString = exchange.getIn().getBody(String.class);
        if (bodyString == null || bodyString.isEmpty()) {
            throw new IllegalArgumentException("Request body is null or empty");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(bodyString);

        String reasonStr = jsonNode.has(OTP_REASON) ? jsonNode.get(OTP_REASON).asText() : null;
        if (reasonStr == null) {
            throw new IllegalArgumentException("reason is required in JSON body");
        }

        String authorization = exchange.getIn().getHeader(AUTHORIZATION, String.class);

        String otpCode = exchange.getIn().getHeader(OTP_CODE_HEADER, String.class);
        if (otpCode == null) {
            throw new IllegalArgumentException("OTP code is required in JSON header");
        }
        User user = (User) AuthenticationUtils.getAuthentication().getPrincipal();
        String mobile = user.getAccessParameters().stream().findFirst().orElse(null);

        if (mobile == null || mobile.isEmpty()) {
            throw new RuntimeException("Mobile number is missing for the logged-in user");
        }

        try {
            OtpReason reason = OtpReason.valueOf(reasonStr.toUpperCase());

            boolean isValid = otpClientService.verifyByParams(
                    otpCode,
                    reason,
                    authorization,
                    mobile
            );

            if (!isValid) {
                throw new RuntimeException("OTP validation failed: Invalid code");
            }

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("OTP validation failed: Invalid reason type");
        }
    }

}