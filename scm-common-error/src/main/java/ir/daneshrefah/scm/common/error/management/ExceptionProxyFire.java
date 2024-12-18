package ir.daneshrefah.scm.common.error.management;

import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Use this class on resolve method of your custom exception resolver to
 * create another exception instead of current exception.
 * The resolver uses a proxy-based mechanism to handle exceptions and converts them into a format
 * understood by the application. Specifically, it throws an exception.
 * </p>
 */
@FunctionalInterface
public interface ExceptionProxyFire {

    static List<Error> fireProxyException(ExceptionProxyFire proxyException) {
        try {
            proxyException.fireProxiedException();
        } catch (Exception e) {
            //prevent from using ResolverProxyException directly.
            if (e instanceof ResolverProxyException) {
                throw new RuntimeException();
            }
            throw new ResolverProxyException(e);
        }
        return null;
    }

    void fireProxiedException();
}
