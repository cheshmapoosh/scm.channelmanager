package ir.daneshrefah.scm.common.model;


public class GatewayOperation {

    private Long id;

    private ServiceType serviceType;
    private String title;
    private String code;

    private GatewayService service;
    private String serviceProviderComponent;
    private String urlBase;
    private String inputJSONSchema;
    private String outputJSONSchema;
    private String metadata;

    public GatewayOperation() {

    }

    public GatewayOperation(String name, String serviceProviderComponent, String urlBase, String inputJSONSchema, String outputJSONSchema, String metadata) {
        this.code = name;
        this.serviceProviderComponent = serviceProviderComponent;
        this.urlBase = urlBase;
        this.inputJSONSchema = inputJSONSchema;
        this.outputJSONSchema = outputJSONSchema;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public GatewayService getService() {
        return service;
    }

    public void setService(GatewayService service) {
        this.service = service;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServiceProviderComponent() {
        return serviceProviderComponent;
    }

    public void setServiceProviderComponent(String serviceProviderComponent) {
        this.serviceProviderComponent = serviceProviderComponent;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    public String getInputJSONSchema() {
        return inputJSONSchema;
    }

    public void setInputJSONSchema(String inputJSONSchema) {
        this.inputJSONSchema = inputJSONSchema;
    }

    public String getOutputJSONSchema() {
        return outputJSONSchema;
    }

    public void setOutputJSONSchema(String outputJSONSchema) {
        this.outputJSONSchema = outputJSONSchema;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
