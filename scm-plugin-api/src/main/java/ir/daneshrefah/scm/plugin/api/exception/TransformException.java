package ir.daneshrefah.scm.plugin.api.exception;

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
        this.transformer = transformer;
    }


    @Override
    public Object getSource() {
        return transformer;
    }

    @Override
    public Object getSourceCode() {
        return transformer;
    }
}
