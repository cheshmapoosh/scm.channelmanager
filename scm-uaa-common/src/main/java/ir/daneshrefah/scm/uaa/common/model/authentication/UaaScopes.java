package ir.daneshrefah.scm.uaa.common.model.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum UaaScopes {
    SESSION_KEY_SCOPE("sessionKey");

    private String scopeCode;

    public static UaaScopes findByScopeCode(String scopeCode)
    {
        return Arrays.stream(UaaScopes.values())
                .filter(s->s.getScopeCode().equals(scopeCode))
                .findFirst()
                .orElse(null);
    }
}
