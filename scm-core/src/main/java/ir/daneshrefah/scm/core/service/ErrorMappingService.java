package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.common.ErrorMappingEntity;
import ir.daneshrefah.scm.core.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.core.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.plugin.api.constants.ErrorCodes;
import ir.daneshrefah.scm.plugin.api.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.model.error.ErrorMapping;
import ir.daneshrefah.scm.plugin.api.model.message.Error;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
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
        if (exception instanceof ConnectException) {
            Error error = new Error(ErrorCodes.ERROR_UNAVAILABLE_PROVIDER, "",
                    message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode());
            message.addError(error, Status.SC_ERROR_UNAVAILABLE_PROVIDER.getCode());
        }
        return message;
    }
    public Message resolveMessageByErrorCode(Message message, BaseException exception) {
        String providerErrorCode = exception.getErrorCode();
        String providerCode = exception.getSource();
        if (null == errorMappings)
            errorMappings = findErrorMappingList();
        Optional<ErrorMapping> errorMappingOptional = errorMappings.stream()
                .filter(person -> (providerCode.equals(person.getServiceComponentProvider().getCode()) &&
                        providerErrorCode.equals(person.getProviderErrorCode())))
                .findFirst();

        if (errorMappingOptional.isPresent()) {
            ErrorMapping errorMapping = errorMappingOptional.get();
            String errorMessage = StringUtils.isEmpty(errorMapping.getMessage()) ? exception.getMessage(): errorMapping.getMessage();
            Error error = new Error(errorMapping.getScmErrorCode(), errorMessage,
                    errorMapping.getServiceComponentProvider().getCode());
            message.addError(error, errorMapping.getStatus().getCode());
        } else {
            Error error = new Error(ErrorCodes.ERROR_UNKNOWN, exception.getMessage(), providerCode);
            message.addError(error, Status.SC_ERROR_SYSTEM.getCode());
        }

        return message;
    }
}
