package ir.daneshrefah.scm.plugin.api.model.message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-01
 */
public class TransformEvent extends Event {

    private String transformerClassName;
    private String errorMessage;
    private Boolean isSuccessful;
    private String outputType;

    public TransformEvent(String transformerClassName, String errorMessage, Boolean isSuccessful, String outputType) {
        this.transformerClassName = transformerClassName;
        this.errorMessage = errorMessage;
        this.isSuccessful = isSuccessful;
        this.outputType = outputType;
    }

    public String getTransformerClassName() {
        return transformerClassName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Boolean getSuccessful() {
        return isSuccessful;
    }

    public String getOutputType() {
        return outputType;
    }
}
