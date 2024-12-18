package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.error.management.ExceptionProxyFire;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class HttpMessageNotReadableExceptionResolver extends ExceptionResolver<HttpMessageNotReadableException>  {

    @Override
    public List<Error> resolve(HttpMessageNotReadableException exception, Locale locale) {
        return ExceptionProxyFire.fireProxyException(()-> {
           throw new MissingRequiredInputException("payload");
        });
    }


}
