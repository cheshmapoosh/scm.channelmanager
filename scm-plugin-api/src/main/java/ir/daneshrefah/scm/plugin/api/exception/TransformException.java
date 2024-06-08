package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
@Getter
public class TransformException extends AbstractBaseException implements ExceptionSourceAware {

    private final AbstractTransformer transformer;

    public TransformException(AbstractTransformer transformer, Throwable cause) {
        super("error on transform data", cause);
        this.transformer = transformer;
    }

    @Override
    public String getSource() {
        return transformer.getClass().getSimpleName();
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }
}
