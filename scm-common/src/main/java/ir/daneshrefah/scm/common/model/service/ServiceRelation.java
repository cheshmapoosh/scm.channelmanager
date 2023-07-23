package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.model.component.ServiceComponent;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class ServiceRelation {

    private ServiceComponent serviceComponent;
    private ServiceRelationType requestTransformerType;
    private ServiceRelationType responseTransformerType;
    private String requestMetadata;
    private String responseMetadata;
    private String requestTransformerClass;
    private String responseTransformerClass;
    private Class<? extends AbstractTransformer> requestTransformer;
    private Class<? extends AbstractTransformer> responseTransformer;

    public ServiceComponent getServiceComponent() {
        return serviceComponent;
    }

    public void setServiceComponent(ServiceComponent serviceComponent) {
        this.serviceComponent = serviceComponent;
    }

    public ServiceRelationType getRequestTransformerType() {
        return requestTransformerType;
    }

    public void setRequestTransformerType(ServiceRelationType requestTransformerType) {
        this.requestTransformerType = requestTransformerType;
    }

    public ServiceRelationType getResponseTransformerType() {
        return responseTransformerType;
    }

    public void setResponseTransformerType(ServiceRelationType responseTransformerType) {
        this.responseTransformerType = responseTransformerType;
    }

    public String getRequestMetadata() {
        return requestMetadata;
    }

    public void setRequestMetadata(String requestMetadata) {
        this.requestMetadata = requestMetadata;
    }

    public String getResponseMetadata() {
        return responseMetadata;
    }

    public void setResponseMetadata(String responseMetadata) {
        this.responseMetadata = responseMetadata;
    }

    public String getRequestTransformerClass() {
        return requestTransformerClass;
    }

    public void setRequestTransformerClass(String requestTransformerClass) {
        this.requestTransformerClass = requestTransformerClass;
    }

    public String getResponseTransformerClass() {
        return responseTransformerClass;
    }

    public void setResponseTransformerClass(String responseTransformerClass) {
        this.responseTransformerClass = responseTransformerClass;
    }

    public Class<? extends AbstractTransformer> getRequestTransformer() {
        return requestTransformer;
    }

    public void setRequestTransformer(Class<? extends AbstractTransformer> requestTransformer) {
        this.requestTransformer = requestTransformer;
    }

    public Class<? extends AbstractTransformer> getResponseTransformer() {
        return responseTransformer;
    }

    public void setResponseTransformer(Class<? extends AbstractTransformer> responseTransformer) {
        this.responseTransformer = responseTransformer;
    }

    public enum ServiceRelationType {

        NONE(1), DYNAMIC(2), JAVA(3);

        ServiceRelationType(Integer code) {
            this.code = code;
        }

        private Integer code;

        public Integer getCode() {
            return code;
        }

        public static ServiceRelationType findByCode(Integer code) {
            for (ServiceRelationType enumValue : ServiceRelationType.values()) {
                if (enumValue.getCode().equals(code)) {
                    return enumValue;
                }
            }
            return null;
        }

    }
}
