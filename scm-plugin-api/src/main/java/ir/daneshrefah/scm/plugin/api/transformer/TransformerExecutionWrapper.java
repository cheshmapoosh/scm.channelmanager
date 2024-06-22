package ir.daneshrefah.scm.plugin.api.transformer;

import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-09-05
 */
public class TransformerExecutionWrapper {

    private TransformerRelation transformerRelation;
    private AbstractJsonTransformer transformerInstance;

    public TransformerExecutionWrapper(TransformerRelation transformerRelation) {
        this.transformerRelation = transformerRelation;
        this.transformerInstance = ClassLoader.findBeanOrCreateInstanceOfClass(
                transformerRelation.getTransformer().getJavaClassName(), AbstractJsonTransformer.class);
    }

    public TransformerRelation getTransformerRelation() {
        return transformerRelation;
    }

    public AbstractJsonTransformer getTransformerInstance() {
        return transformerInstance;
    }
}
