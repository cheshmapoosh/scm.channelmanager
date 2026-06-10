package ir.daneshrefah.scm.uaa.common.core;

import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class UserDetailsKeyResolvingCache implements Cache {
    private static final String KEY_SEPARATOR = "::";

    private final Cache delegate;
    private final String keyExpression;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public UserDetailsKeyResolvingCache(Cache delegate) {
        this(delegate, null);
    }

    public UserDetailsKeyResolvingCache(Cache delegate, String keyExpression) {
        this.delegate = delegate;
        this.keyExpression = keyExpression;
    }

    @Override
    @NonNull
    public String getName() {
        return delegate.getName();
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        return delegate.get(key);
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        return delegate.get(key, type);
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        delegate.put(resolveWriteKey(key, value), value);
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, Object value) {
        return delegate.putIfAbsent(resolveWriteKey(key, value), value);
    }

    @Override
    public void evict(@NonNull Object key) {
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
        if (StringUtils.hasText(keyExpression)) {
            Expression expression = expressionParser.parseExpression(keyExpression);
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
