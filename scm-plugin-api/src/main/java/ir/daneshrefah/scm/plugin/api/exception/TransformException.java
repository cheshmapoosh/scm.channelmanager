package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
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
public class TransformException extends BaseException {

    private AbstractTransformer transformer;

    public TransformException(AbstractTransformer transformer, Throwable cause) {
        super("error on transform data", cause);
        this.transformer = transformer;
    }

}
