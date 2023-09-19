package ir.daneshrefah.scm.common.model.message;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-01
 */
public class TransformEvent extends Event {

    private String transformerClassName;
    private String outputType;
    private String invokerClassName;


    public TransformEvent(LocalDateTime startTime, LocalDateTime endTime, Object error, Object input, Object output, Boolean isSuccessful) {
        super(EventType.TRANSFORM, startTime, endTime, error, input, output, isSuccessful);
    }

    public String getTransformerClassName() {
        return transformerClassName;
    }

    public void setTransformerClassName(String transformerClassName) {
        this.transformerClassName = transformerClassName;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getInvokerClassName() {
        return invokerClassName;
    }

    public void setInvokerClassName(String invokerClassName) {
        this.invokerClassName = invokerClassName;
    }
}
