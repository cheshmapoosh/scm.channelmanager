package ir.daneshrefah.scm.plugin.pichack.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ConstantService;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractRestExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.api.transformer.ServiceCodeLookupTransformer;
import ir.daneshrefah.scm.plugin.pichack.transformer.PichackChequeRegisterRequestTransformer;
import ir.daneshrefah.scm.plugin.pichack.transformer.PichackChequeRegisterResponseTransformer;
import ir.daneshrefah.scm.plugin.pichack.util.PichakUtil;
import ir.daneshrefah.scm.utils.base64.Base64Utils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ir.daneshrefah.scm.plugin.pichack.util.Constants.*;
import static ir.daneshrefah.scm.utils.string.HttpConstants.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@Component
public final class PichackServiceProvider extends AbstractRestExternalServiceProvider {

    private static final String PICHACK_CALLER_TERMINAL_NAME_HEADER = "callerTerminalName";
    private static final String PICHACK_CALLER_BRANCH_CODE_HEADER = "callerBranchCode";
    private static final String PICHACK_CALLER_BRANCH_USERNAME_HEADER = "callerBranchUserName";
    private static final String PICHACK_CUSTOMER_AUTH_STATUS_HEADER = "customerAuthStatus";
    private static final String PICHACK_WEBSERVICE_AUTHORIZATION_USERNAME = "pichack_webservice_authorization_username";
    private static final String PICHACK_WEBSERVICE_AUTHORIZATION_PASSWORD = "pichack_webservice_authorization_password";

    private final ConstantService constantService;

    public PichackServiceProvider(ProducerTemplate producerTemplate, CamelContext camelContext,
                                  ResourceService resourceService, ObjectMapper objectMapper, ConstantService constantService) {
        super(producerTemplate, camelContext, resourceService, objectMapper);
        this.constantService = constantService;
    }

    @Override
    @SneakyThrows
    protected String prepareTargetUrl(Message message) {
        ExternalService service = (ExternalService) message.getHeader().getServiceAccess().getService();
        String providerEndpoint = extractProviderEndpoint();
        JsonNode componentMetadata = service.getMetadata();
        String target = providerEndpoint + StringUtils.removeStart(componentMetadata.get("serviceName").asText(), "/");
        return target;
    }

    @Override
    protected List<AbstractTransformer> prepareRequestTransformers() {
        Map<String, AbstractTransformer> transformerMap = new HashMap<>();
        transformerMap.put(SERVICE_CODE_CHEQUE_REGISTER, new PichackChequeRegisterRequestTransformer());
        transformerMap.put(SERVICE_CODE_CHEQUE_CONFIRM_BY_RECEIVER, null);
        transformerMap.put(SERVICE_CODE_CHEQUE_TRANSFER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_HOLDER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_ISSUER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_CHECK_RECEIVER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_CHECK_PARAM, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_TRANSFERS_CHAIN, null);
        return List.of(new ServiceCodeLookupTransformer(transformerMap));
    }

    @Override
    protected List<AbstractTransformer> prepareResponseTransformers() {
        Map<String, AbstractTransformer> transformerMap = new HashMap<>();
        transformerMap.put(SERVICE_CODE_CHEQUE_REGISTER, new PichackChequeRegisterResponseTransformer());
        transformerMap.put(SERVICE_CODE_CHEQUE_CONFIRM_BY_RECEIVER, null);
        transformerMap.put(SERVICE_CODE_CHEQUE_TRANSFER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_HOLDER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_ISSUER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_CHECK_RECEIVER, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_CHECK_PARAM, null);
        transformerMap.put(SERVICE_CODE_INQUIRY_BY_TRANSFERS_CHAIN, null);
        return List.of(new ServiceCodeLookupTransformer(transformerMap));
    }

    @Override
    protected Map<String, ?> extractAdditionalHeaders(Message message) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HTTP_HEADER_CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_JSON);
        headers.put(HTTP_HEADER_AUTHORIZATION, createAuthorizationData());

        String cmTerminalName = PichakUtil.provideTerminalName(message);
        String cmBranchCode = PichakUtil.provideBranchCode(message);
        String cmBranchUsername = PichakUtil.provideBranchUsername(message);
        String customerAuthStatus = PichakUtil.provideCustomerAuthStatus(message);

        if (null != cmTerminalName)
            headers.put(PICHACK_CALLER_TERMINAL_NAME_HEADER, cmTerminalName);
        if (null != cmBranchCode)
            headers.put(PICHACK_CALLER_BRANCH_CODE_HEADER, cmBranchCode);
        if (null != cmBranchUsername)
            headers.put(PICHACK_CALLER_BRANCH_USERNAME_HEADER, cmBranchUsername);
        if (null != customerAuthStatus)
            headers.put(PICHACK_CUSTOMER_AUTH_STATUS_HEADER, customerAuthStatus);

        return headers;
    }

    private String createAuthorizationData() {
        String username = constantService.findConstantValueByKey(PICHACK_WEBSERVICE_AUTHORIZATION_USERNAME).orElse(null);
        String password = constantService.findConstantValueByKey(PICHACK_WEBSERVICE_AUTHORIZATION_PASSWORD).orElse(null);
        if (StringUtils.isEmpty(username)) {
            username = "user";
            password = "password";
        }
        String auth = username + StringUtils.COLON + password;
        String encodedAuth = Base64Utils.encodeWithBase64(auth);

        return "Basic " + encodedAuth;
    }

}
