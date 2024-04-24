package ir.daneshrefah.scm.core.service;

import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.exception.AbstractValidationException;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ErrorCodeAwareException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.entity.common.ErrorMappingEntity;
import ir.daneshrefah.scm.core.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.core.mapper.ExceptionMapper;
import ir.daneshrefah.scm.core.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.plugin.api.exception.ProviderErrorResponseException;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.model.error.ErrorMapping;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
@Service
public class ErrorHandlerServiceImpl extends ErrorHandlerService {

    @Autowired
    private ErrorMappingRepository repository;
    private List<ErrorMapping> errorMappings;

    public List<ErrorMapping> listErrorMappings() {
        if (null == errorMappings) {
            Iterable<ErrorMappingEntity> errorMappingEntities = repository.findAll();
            errorMappings = ErrorMappingMapper.INSTANCE.entitiesToModels(errorMappingEntities);
        }
        return errorMappings;
    }

    @Override
    public Message resolveMessageByValidationMessage(Message message, Set<ValidationMessage> errors) {
        for (Iterator<ValidationMessage> iterator = errors.iterator(); iterator.hasNext(); ) {
            ValidationMessage validationMessage = iterator.next();
            Error error = new Error(validationMessage.getProperty(),
                    validationMessage.getCode(), validationMessage.getMessage(), null);
            message.addError(error, MessageStatus.SC_ERROR_VALIDATION);
        }
        return message;
    }

    @Override
    public Message resolveMessageByException(Message message, Exception exception) {
        if (exception instanceof InvocationTargetException && null != exception.getCause()) {
            return resolveMessageByException(message, (Exception) exception.getCause());
        }
        if (exception.getClass().isAssignableFrom(RuntimeException.class) && null != exception.getCause()) {
            return resolveMessageByException(message, (Exception) exception.getCause());
        }
        if (null == message) {
            message = createEmptyMessage(null, null);
        }
        if (exception instanceof DataIntegrityViolationException) {
            DataIntegrityViolationException ex = (DataIntegrityViolationException) exception;
            Error error = null;
            if (ex.getMessage().contains("SQLCODE=-803") || ex.getMessage().contains("Duplicate")) {
                error = new Error("Duplicate", ERROR_CODE_DUPLICATE_RECORD, ex.getMessage(), exception);
            }
            if (null == error) {
                error = new Error("Integrity", ERROR_CODE_VIOLATION_DATA_INTEGRITY, ex.getMessage(), exception);
            }
            message.addError(error, MessageStatus.SC_ERROR_DATA_INTEGRITY_VIOLATION);
            return message;
        }
        if (exception instanceof ProviderErrorResponseException) { // TODO must check table
            final ProviderErrorResponseException providerException = (ProviderErrorResponseException) exception;
            String errorCode = providerException.getRemoteErrorCode();
            Optional<ErrorMapping> mapping = listErrorMappings().stream()
                    .filter(errorMapping -> (errorMapping.getProvider().getCode().equals(providerException.getProviderCode())) &&
                            errorMapping.getProviderErrorCode().equals(errorCode))
                    .findFirst();
            Error error = null;
            if (mapping.isPresent()) {
                String errorMessage = StringUtils.isNotEmpty(mapping.get().getMessage()) ? mapping.get().getMessage() :
                        providerException.getErrorMessage();
                error = new Error(providerException.getProviderCode(), mapping.get().getScmErrorCode(), errorMessage, exception);
            } else {
                String remoteErrorCode = providerException.getRemoteErrorCode();
                String remoteErrorMessage = providerException.getErrorMessage();
                String errorMessage = String.format("invalid error received from provider : %s, remoteErrorCode: %s, remoteMessage: %s",
                        providerException.getProviderCode(), remoteErrorCode, remoteErrorMessage);
                error = new Error(providerException.getProviderCode(), ERROR_CODE_INVALID_REMOTE_RESPONSE, errorMessage, exception);
            }
            message.addError(error, MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
            return message;
        }

        if (exception instanceof AbstractValidationException) {
            AbstractValidationException validationException = (AbstractValidationException) exception;
            String source = validationException.getSource();
            String errorMessage = validationException.getMessage();
            Error error = new Error(source, validationException.getErrorCode(), errorMessage, exception);
            message.addError(error, MessageStatus.SC_ERROR_VALIDATION);
            return message;
        }

        if (exception instanceof ErrorCodeAwareException) {
            ErrorCodeAwareException awareException = (ErrorCodeAwareException) exception;
            String source = awareException.getSource();
            String errorMessage = awareException.getMessage();
            Error error = new Error(source, awareException.getErrorCode(), errorMessage, exception);
            message.addError(error, awareException.getStatus());
            return message;
        }

        if (exception instanceof NoMatchRecordFoundException) {
            NoMatchRecordFoundException resultNotFoundException = (NoMatchRecordFoundException) exception;
            String source = resultNotFoundException.getSource();
            Integer errorCode = resultNotFoundException.getErrorCode();
            String errorMessage = resultNotFoundException.getMessage();
            Error error = new Error(source, errorCode, errorMessage, exception);
            message.addError(error, MessageStatus.SC_NOT_FOUND);
            return message;
        }

        Optional<ExceptionMapper> mapper = ExceptionMapper.findByException(exception.getClass());
        if (mapper.isPresent()) {
            Error error = new Error(exception instanceof BaseException ? ((BaseException) exception).getSource() : null,
                    mapper.get().getErrorCode(),
                    null != exception.getCause() ? exception.getCause().getMessage() : exception.getMessage(), exception);
            message.addError(error, mapper.get().getStatus());
        } else {
            Error error = new Error(null, ErrorCodes.ERROR_CODE_SYSTEM_ERROR,
                    null != exception.getCause() ? exception.getCause().getMessage() : exception.getMessage(), exception);
            message.addError(error, MessageStatus.SC_ERROR_SYSTEM);
        }

        return message;
    }

    /*@Override
    public BaseException resolveExceptionByError(Message message) {
        List<Error> errors = message.getErrors();
        if (null == errors || errors.isEmpty()) {
            return null;
        }
        return new ServiceExecutionException(message.getHeader().getServiceAccess().getService(),
                errors.get(0).getErrorCode(), errors.get(0).getMessage());
    }*/

    private Message createEmptyMessage(MessageBuildRequest request, TerminalServiceAccess serviceAccess) {
//        Header header = Header.builder().request(new MessageRequestInfo(request)).serviceAccess(serviceAccess).build();
//        Message result = Message.builder()
//                .header(header)
//                .build();
//        return result;
        return null;
    }

}
