package ir.daneshrefah.scm.core.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorType;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.core.entity.common.ErrorMappingEntity;
import ir.daneshrefah.scm.core.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.core.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.plugin.api.exception.*;
import ir.daneshrefah.scm.plugin.api.model.error.ErrorMapping;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
@Service
public class ErrorMappingService {

    @Autowired
    private ErrorMappingRepository repository;
    private List<ErrorMapping> errorMappings;

    public List<ErrorMapping> listErrorMappings() {
        if (null == errorMappings) {
            Iterable<ErrorMappingEntity> errorMappingEntities = repository.findAll();
            errorMappings =  ErrorMappingMapper.INSTANCE.entitiesToModels(errorMappingEntities);
        }
        return errorMappings;
    }

    public Message resolveMessageByValidationMessage(Message message, Set<ValidationMessage> errors) {
        for (Iterator<ValidationMessage> iterator = errors.iterator(); iterator.hasNext(); ) {
            ValidationMessage validationMessage = iterator.next();
            Error error = new Error(ErrorType.VALIDATION, null, validationMessage.getPath(),
                    validationMessage.getCode(), validationMessage.getMessage());
            message.addError(error, Status.SC_ERROR_VALIDATION);
        }
        return message;
    }

    public Message resolveMessageByException(Message message, Exception exception) {
        String providerCode = StringUtils.EMPTY;
        Terminal terminal = message.getHeader().getService().getTerminalServiceAccess().getTerminal();
        ir.daneshrefah.scm.common.model.service.Service service = message.getHeader().getService().getTerminalServiceAccess().getService();
        if (service instanceof ExternalService) {
            providerCode = ((ExternalService) service).getServiceProvider().getCode();
        }
        String finalProviderCode = providerCode;

        if (exception instanceof ProviderUnreachableException) {
            ProviderUnreachableException providerUnreachableException = (ProviderUnreachableException) exception;
            Error error = new Error(ErrorType.HOST_UNREACHABLE, providerUnreachableException.getProvider().getCode(),
                    null, ErrorType.HOST_UNREACHABLE.getCode(), providerUnreachableException.getCause().getMessage());
            message.addError(error, Status.SC_ERROR_UNREACHABLE_PROVIDER);
            return message;
        }
        if (exception instanceof TransformException) {
            TransformException transformException = (TransformException) exception;
            Error error = new Error(ErrorType.SYSTEM_ERROR, transformException.getTransformer().getClass().getSimpleName(),
                    null, ErrorType.SYSTEM_ERROR.getCode(), transformException.getCause().getMessage());
            message.addError(error, Status.SC_ERROR_SYSTEM);
            message.nullPayload();
            return message;
        }
        if (exception instanceof InvalidProviderResponseException) {
            InvalidProviderResponseException invalidProviderResponseException = (InvalidProviderResponseException) exception;
            Error error = new Error(ErrorType.INVALID_PROVIDER_RESPONSE, invalidProviderResponseException.getProvider().getCode(),
                    null, ErrorType.INVALID_PROVIDER_RESPONSE.getCode(), invalidProviderResponseException.getCause().getMessage());
            message.addError(error, Status.SC_ERROR_UNREACHABLE_PROVIDER);
            return message;
        }
        if (exception instanceof ProviderUnSuccessfulResponseException) {
            ProviderUnSuccessfulResponseException providerUnSuccessfulResponseException = (ProviderUnSuccessfulResponseException) exception;
            Error error = new Error(ErrorType.INVALID_PROVIDER_RESPONSE, providerUnSuccessfulResponseException.getProvider().getCode(),
                    null, ErrorType.INVALID_PROVIDER_RESPONSE.getCode(), "invalid status code: " + providerUnSuccessfulResponseException.getStatusCode());
            message.addError(error, Status.SC_ERROR_UNREACHABLE_PROVIDER);
            return message;
        }
        if (exception instanceof ProviderUnknownException) {
            ProviderUnknownException providerUnknownException = (ProviderUnknownException) exception;
            Error error = new Error(ErrorType.INVALID_PROVIDER_RESPONSE, providerUnknownException.getProvider().getCode(),
                    null, ErrorType.INVALID_PROVIDER_RESPONSE.getCode(), providerUnknownException.getMessage());
            message.addError(error, Status.SC_ERROR_UNREACHABLE_PROVIDER);
            return message;
        }
        if (exception instanceof ProviderErrorResponseException) {
            ProviderErrorResponseException providerErrorResponseException = (ProviderErrorResponseException) exception;
            String errorCode = providerErrorResponseException.getErrorCode();
            Optional<ErrorMapping> mapping = listErrorMappings().stream()
                    .filter(errorMapping -> (errorMapping.getExceptionClassName().equals(exception.getClass())) &&
                            (null == errorMapping.getProvider() || errorMapping.getProvider().getCode().equals(finalProviderCode)) &&
                            errorMapping.getProviderErrorCode().equals(errorCode))
                    .findFirst();
            Error error = null;
            if (mapping.isPresent()) {
                String errorMessage = StringUtils.isNotEmpty(mapping.get().getMessage()) ? mapping.get().getMessage() :
                        providerErrorResponseException.getErrorMessage();
                error = new Error(ErrorType.VALIDATION, providerCode,
                        null, mapping.get().getScmErrorCode(), errorMessage);
            } else {
                error = new Error(ErrorType.VALIDATION, providerCode,
                        null, errorCode, providerErrorResponseException.getErrorMessage());
            }
            message.addError(error, Status.SC_ERROR_VALIDATION);
            return message;
        }

        return message;
    }

}
