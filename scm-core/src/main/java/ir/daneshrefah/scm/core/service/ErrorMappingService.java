package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.common.ErrorMappingEntity;
import ir.daneshrefah.scm.core.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.core.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.plugin.api.constants.ErrorCodes;
import ir.daneshrefah.scm.plugin.api.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.exception.ExternalProviderException;
import ir.daneshrefah.scm.plugin.api.exception.TransformException;
import ir.daneshrefah.scm.plugin.api.exception.ValidationException;
import ir.daneshrefah.scm.plugin.api.model.error.ErrorMapping;
import ir.daneshrefah.scm.plugin.api.model.message.Error;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<ErrorMapping> findErrorMappingList() {
        Iterable<ErrorMappingEntity> errorMappingEntities = repository.findAll();
        return ErrorMappingMapper.INSTANCE.entitiesToModels(errorMappingEntities);
    }

    public Message resolveMessageByException(Message message, Exception exception) {

        if (exception instanceof TransformException) {
            TransformException transformException = (TransformException) exception;
            Exception cause = null != transformException.getCause() ? (Exception) transformException.getCause() : transformException;
            cause = ClassLoader.cloneExceptionWithoutStackTrace(cause);
            String errorMessage = null != transformException.getMessage() ? transformException.getMessage() : "error on transform message";
            Error error = new Error(ErrorCodes.ERROR_TRANSFORMATION, errorMessage,
                    transformException.getSource(), null, null, cause);
            message.addError(error, Status.SC_ERROR_SYSTEM);
            return message;
        }

        if (exception instanceof ValidationException) {
            ValidationException validationException = (ValidationException) exception;
            String errorCode = null != validationException.getErrorCode() ? validationException.getErrorCode() : ErrorCodes.ERROR_VALIDATION;
            String errorMessage = null != validationException.getMessage() ? validationException.getMessage() : "error on validation";
            Exception cause = null != validationException.getCause() ? (Exception) validationException.getCause() : validationException;
            cause = ClassLoader.cloneExceptionWithoutStackTrace(cause);
            Error error = new Error(errorCode, errorMessage,
                    validationException.getSource(), null, null, cause);
            message.addError(error, Status.SC_ERROR_VALIDATION);
            return message;
        }

        Optional<ErrorMapping> errorMappingOptional = findErrorMappingByException(exception);
        if (errorMappingOptional.isPresent()) {
            ErrorMapping errorMapping = errorMappingOptional.get();
            String errorMessage = StringUtils.isEmpty(errorMapping.getMessage()) ? exception.getMessage() : errorMapping.getMessage();
            Error error = new Error(errorMapping.getScmErrorCode(), errorMessage,
                    errorMapping.getExternalServiceProvider().getCode(), errorMapping.getProviderErrorCode(),
                    null, exception);
            message.addError(error, errorMapping.getStatus());
            return message;
        }

        Object source = exception instanceof BaseException ? ((BaseException) exception).getSource() : null;
        Error error = new Error(ErrorCodes.ERROR_UNKNOWN, exception.getMessage(),
                source, null,null, exception);
        message.addError(error, Status.SC_ERROR_SYSTEM);

        return message;
    }

    private Optional<ErrorMapping> findErrorMappingByException(Exception exception) {
        if (null == errorMappings) {
            errorMappings = findErrorMappingList();
        }
        Optional<ErrorMapping> errorMappingOptional = null;
        if (exception instanceof ExternalProviderException) {
            ExternalProviderException providerException = (ExternalProviderException) exception;
            errorMappingOptional = errorMappings.stream()
                    .filter(errorMapping -> (providerException.getSource().equals(errorMapping.getExternalServiceProvider().getCode()) &&
                            providerException.getSourceErrorCode().equals(errorMapping.getProviderErrorCode())))
                    .findFirst();
        }
        if (errorMappingOptional.isEmpty()) {
            errorMappingOptional = errorMappings.stream()
                    .filter(errorMapping -> (exception.getClass().getName().equals(errorMapping.getExceptionClassName()) &&
                            null == errorMapping.getExternalServiceProvider()))
                    .findFirst();
        }
        return errorMappingOptional;
    }

    private Optional<ErrorMapping> findErrorMappingByBaseException(BaseException exception) {
        return null;
    }

    private Message resolveMessageByErrorCode(Message message, BaseException exception) {
//        String providerErrorCode = exception.getErrorCode();
//        Object providerCode = exception.getSource();
//        if (null == errorMappings)
//            errorMappings = findErrorMappingList();
//        Optional<ErrorMapping> errorMappingOptional = errorMappings.stream()
//                .filter(person -> (providerCode.equals(person.getExternalServiceProvider().getCode()) &&
//                        providerErrorCode.equals(person.getProviderErrorCode())))
//                .findFirst();
//
//        if (errorMappingOptional.isPresent()) {
//            ErrorMapping errorMapping = errorMappingOptional.get();
//            String errorMessage = StringUtils.isEmpty(errorMapping.getMessage()) ? exception.getMessage(): errorMapping.getMessage();
//            Error error = new Error(errorMapping.getScmErrorCode(), errorMessage,
//                    errorMapping.getExternalServiceProvider().getCode(), providerErrorCode);
//            message.addError(error, errorMapping.getStatus().getCode());
//        } else {
//            Error error = new Error(ErrorCodes.ERROR_UNKNOWN, exception.getMessage(), providerCode, providerErrorCode);
//            message.addError(error, Status.SC_ERROR_SYSTEM.getCode());
//        }

        return message;
    }
}
