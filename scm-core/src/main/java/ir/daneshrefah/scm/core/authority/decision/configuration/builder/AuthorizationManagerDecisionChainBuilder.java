package ir.daneshrefah.scm.core.authority.decision.configuration.builder;

import ir.daneshrefah.scm.core.authority.decision.configuration.model.AuthorizationManagerChainDefinition;
import ir.daneshrefah.scm.core.authority.decision.configuration.model.SecurityContext;
import ir.daneshrefah.scm.core.authority.decision.manager.AuthorizationManagerTypes;
import org.springframework.security.authorization.AuthorizationManager;

import java.beans.Introspector;

public record AuthorizationManagerDecisionChainBuilder(AuthorizationManagerChainDefinition chainDefinition) {

    public static AuthorizationManagerDecisionChainBuilder createWithType(String managerBeanName) {
        AuthorizationManagerDecisionChainBuilder chainBuilder = new AuthorizationManagerDecisionChainBuilder(new AuthorizationManagerChainDefinition());
        chainBuilder.chainDefinition.setManagerBeanName(managerBeanName);
        return chainBuilder;
    }

    public static AuthorizationManagerDecisionChainBuilder createWithType(Class<?> managerBean) {
        AuthorizationManagerDecisionChainBuilder chainBuilder = new AuthorizationManagerDecisionChainBuilder(new AuthorizationManagerChainDefinition());
        String beanName = Introspector.decapitalize(managerBean.getSimpleName());
        chainBuilder.chainDefinition.setManagerBeanName(beanName);
        return chainBuilder;
    }

    public static AuthorizationManagerDecisionChainBuilder createWithAffirmativeManger() {
        return createWithType(AuthorizationManagerTypes.AFFIRMATIVE.getBeanName());
    }

    public static AuthorizationManagerDecisionChainBuilder createWithConsensusManger() {
        return createWithType(AuthorizationManagerTypes.CONSENSUS.getBeanName());
    }

    public static AuthorizationManagerDecisionChainBuilder createWithUnanimousManger() {
        return createWithType(AuthorizationManagerTypes.UNANIMOUS.getBeanName());
    }


    public AuthorizationManagerDecisionChainBuilder register(Class<? extends AuthorizationManager<SecurityContext>> managerClass) {
        this.chainDefinition.getAuthorities().add(managerClass);
        return this;
    }

    public AuthorizationManagerDecisionChainBuilder registerDynamically() {
        this.chainDefinition.setDynamicAuthorization(true);
        return this;
    }

    public AuthorizationManagerChainDefinition build() {
        return chainDefinition;
    }
}
