package ir.daneshrefah.scm.process.exception.schema;

import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JsonSchemaExceptionResolver extends ExceptionResolver<JsonSchemaException> {
    private final ErrorMappingService errorMappingService;

    @Override
    public List<Error> resolve(JsonSchemaException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionByClassName(exception.getClass().getName()).orElseThrow(RuntimeException::new);
        List<Error> errors = new ArrayList<>();
        errors.add(new Error(exception.getSource(), errorMapping.getScmErrorCode(), exception.getMessage(), errorMapping.getStatus(), exception));
        return errors;
    }
}
