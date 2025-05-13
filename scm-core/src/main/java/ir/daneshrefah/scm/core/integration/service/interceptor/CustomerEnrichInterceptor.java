package ir.daneshrefah.scm.core.integration.service.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.NoAssetFoundException;
import ir.daneshrefah.scm.common.exception.NoCustomerFoundException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.InterceptorConfig;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractAuditableExternalService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@RequiredArgsConstructor
@Component
public class CustomerEnrichInterceptor extends MessageInterceptor {

    private final PersonProfileLoader personProfileLoader;
    private final ObjectMapper objectMapper;

    @Override
    protected Message internalIntercept(Message message) {
        Service serviceAccess = message.getHeader().getService();
        AbstractAuditableExternalService service = serviceAccess instanceof AbstractAuditableExternalService ?
                (AbstractAuditableExternalService) serviceAccess : null;
        if (Objects.isNull(service) || Objects.isNull(service.getServiceProvider().getAssetProvider()) ||
                !AuthenticationUtils.isFullyAuthenticated()) {
            throw new NoCustomerFoundException();
        }

        if (!isLoadAssetRequired(serviceAccess)) {
            return message;
        }

        UserProfile profile = personProfileLoader.preparePersonProfileMemberships(AuthenticationUtils.getScmAuthentication());
        if (Objects.isNull(profile) || !profile.hasMembership(service.getServiceProvider().getAssetProvider().getId())) {
            throw new NoAssetFoundException();
        }

        return message;
    }

    @Override
    protected boolean support(Service service) {
        return  isLoadAssetRequired(service);
    }

    @Override
    public InterceptorConfig interceptorConfig() {
        return InterceptorConfig
                .create()
                .order(5)
                .type(InterceptorConfig.Type.REQUEST)
                .build();
    }

    private boolean isLoadAssetRequired(Service service) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        return service.getCheckAccessAsset() && terminal.isSupportCheckAssetAccess();
    }


}
