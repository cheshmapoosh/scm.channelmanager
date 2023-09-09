package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
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
    private AbstractTransformer transformerInstance;

    public TransformerExecutionWrapper(TransformerRelation transformerRelation) {
        this.transformerRelation = transformerRelation;
        this.transformerInstance = ClassLoader.findBeanOrCreateInstanceOfClass(
                transformerRelation.getTransformer().getJavaClassName(), AbstractTransformer.class);
    }

    public TransformerRelation getTransformerRelation() {
        return transformerRelation;
    }

    public AbstractTransformer getTransformerInstance() {
        return transformerInstance;
    }
}
