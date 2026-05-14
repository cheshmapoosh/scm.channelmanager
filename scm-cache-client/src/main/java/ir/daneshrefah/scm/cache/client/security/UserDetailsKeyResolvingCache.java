package ir.daneshrefah.scm.cache.client.security;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class UserDetailsKeyResolvingCache implements Cache {

    private static final String KEY_SEPARATOR = "::";

    private final Cache delegate;
    private final CacheClientProperties.UserCache properties;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        return delegate.get(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return delegate.get(key, type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        delegate.put(resolveWriteKey(key, value), value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return delegate.putIfAbsent(resolveWriteKey(key, value), value);
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    private Object resolveWriteKey(Object key, Object value) {
        if (!(value instanceof UserDetails userDetails)) {
            return key;
        }
        return resolveUserDetailsKey(userDetails);
    }

    private String resolveUserDetailsKey(UserDetails user) {
        if (StringUtils.hasText(properties.getKeyExpression())) {
            Expression expression = expressionParser.parseExpression(properties.getKeyExpression());
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("user", user);
            Object value = expression.getValue(context);
            if (value == null || !StringUtils.hasText(String.valueOf(value))) {
                throw new IllegalStateException("User cache key expression returned blank value");
            }
            return String.valueOf(value);
        }

        String terminalCode = extractNestedTerminalCode(user);
        if (StringUtils.hasText(terminalCode)) {
            return user.getUsername() + KEY_SEPARATOR + terminalCode;
        }
        return user.getUsername();
    }

    private String extractNestedTerminalCode(UserDetails user) {
        try {
            Method getUserMethod = user.getClass().getMethod("getUser");
            Object domainUser = getUserMethod.invoke(user);
            if (domainUser == null) {
                return null;
            }
            Method getTerminalCodeMethod = domainUser.getClass().getMethod("getTerminalCode");
            Object terminalCode = getTerminalCodeMethod.invoke(domainUser);
            return terminalCode == null ? null : String.valueOf(terminalCode);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
