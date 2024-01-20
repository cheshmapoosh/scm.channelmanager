package ir.daneshrefah.scm.core.service;

import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.core.entity.common.ErrorMappingEntity;
import ir.daneshrefah.scm.core.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.core.mapper.ExceptionMapper;
import ir.daneshrefah.scm.core.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceExecutionException;
import ir.daneshrefah.scm.plugin.api.exception.ProviderErrorResponseException;
import ir.daneshrefah.scm.plugin.api.model.error.ErrorMapping;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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
            Error error = new Error(validationMessage.getPath(),
                    validationMessage.getCode(), validationMessage.getMessage());
            message.addError(error, Status.SC_ERROR_VALIDATION);
        }
        return message;
    }

    public Message resolveMessageByException(Message message, Exception exception) {
        String providerCode = StringUtils.EMPTY;
        Terminal terminal = message.getHeader().getServiceAccess().getTerminal();
        ir.daneshrefah.scm.common.model.service.Service service = message.getHeader().getServiceAccess().getService();
        if (service instanceof ExternalService) {
            providerCode = ((ExternalService) service).getServiceProvider().getCode();
        }
        String finalProviderCode = providerCode;

        if (exception instanceof ProviderErrorResponseException) {
            ProviderErrorResponseException providerErrorResponseException = (ProviderErrorResponseException) exception;
            String errorCode = providerErrorResponseException.getErrorCode();
            Optional<ErrorMapping> mapping = listErrorMappings().stream()
                    .filter(errorMapping -> (errorMapping.getProvider().getCode().equals(finalProviderCode)) &&
                            errorMapping.getProviderErrorCode().equals(errorCode))
                    .findFirst();
            Error error = null;
            if (mapping.isPresent()) {
                String errorMessage = StringUtils.isNotEmpty(mapping.get().getMessage()) ? mapping.get().getMessage() :
                        providerErrorResponseException.getErrorMessage();
                error = new Error(providerCode, mapping.get().getScmErrorCode(), errorMessage);
            } else {
                error = new Error(providerCode, errorCode, providerErrorResponseException.getErrorMessage());
            }
            message.addError(error, Status.SC_ERROR_VALIDATION);
            return message;
        }

        if (exception instanceof ValidationException) {
            ValidationException validationException = (ValidationException) exception;
            String source = validationException.getSource();
            Integer errorCode = null != validationException.getErrorCode() ? validationException.getErrorCode() : ErrorCodes.ERROR_CODE_VALIDATION;
            String errorMessage = validationException.getMessage();
            Error error = new Error(source, errorCode, errorMessage);
            message.addError(error, Status.SC_ERROR_VALIDATION);
            message.nullPayload();
            return message;
        }

        if (exception instanceof JavaServiceExecutionException) {
            Exception e = (Exception) exception.getCause();
            if (e instanceof DataIntegrityViolationException) {
                DataIntegrityViolationException ex = (DataIntegrityViolationException) e;
                Error error = null;
                if (ex.getMessage().contains("SQLCODE=-803")) {
                    error = new Error(((JavaServiceExecutionException) exception).getSource(), ERROR_CODE_DUPLICATE_RECORD, "recode is duplicate");
                }
                if (null == error) {
                    error = new Error(((JavaServiceExecutionException) exception).getSource(), ERROR_CODE_DATA_INTEGRITY_VIOLATION, ex.getMessage());
                }
                message.addError(error, Status.SC_ERROR_DATA_INTEGRITY_VIOLATION);
                message.nullPayload();
                return message;
            }
        }
        Optional<ExceptionMapper> mapper = ExceptionMapper.findByException(exception.getClass());
        if (mapper.isPresent()) {
            Error error = new Error(exception instanceof BaseException ? ((BaseException) exception).getSource() : null,
                    mapper.get().getErrorCode(),
                    null != exception.getCause() ? exception.getCause().getMessage() : exception.getMessage());
            message.addError(error, mapper.get().getStatus());
        } else {
            Error error = new Error(null, ErrorCodes.ERROR_CODE_SYSTEM_ERROR,
                    null != exception.getCause() ? exception.getCause().getMessage() : exception.getMessage());
            message.addError(error, Status.SC_ERROR_SYSTEM);
            message.nullPayload();
        }

        return message;
    }

}
