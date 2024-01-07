package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
public class TransformException extends BaseException {

    private AbstractTransformer transformer;

    public TransformException(AbstractTransformer transformer) {
        super(null, null);
        this.transformer = transformer;
    }

}
