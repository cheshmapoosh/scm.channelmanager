package ir.daneshrefah.scm.core.authority.decision.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationManager;

/**
 * @see Vote
 * @see AuthorizationManager
 */
@Getter
@RequiredArgsConstructor
public enum AuthorizationManagerTypes {
    /***
     *  Accepted if 'ACCESS_GRANTED' are more than 'ACCESS_DENIED', if both are same then 'ACCESS_GRANTED' wins.
     */
    CONSENSUS("consensusBasedAuthorizationManager"),
    /**
     * Grants access with the first 'ACCESS_GRANTED' and deny with any 'ACCESS_DENIED' vote.
     */
    AFFIRMATIVE("affirmativeBasedAuthorizationManager"),
    /**
     * Rejected by first 'ACCESS_DENIED'
     */
    UNANIMOUS("unanimousAuthorizationManager");

    private final String beanName;
}
