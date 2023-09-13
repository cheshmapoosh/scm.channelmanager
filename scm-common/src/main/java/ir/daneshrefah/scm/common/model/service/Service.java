package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class Service extends BaseModel<String> {

    private String code;
    private String title;
    private String alias;
    private Integer version;
    private Boolean isSystemic;
    private String metadata;
    private ServiceType type;
    private Service parent;
    private ServiceImplementationType implementationType;
    private String requestJsonSchema;
    private String responseJsonSchema;
    private Boolean checkAccessFirstAuthentication;
    private Boolean checkAccessSecondAuthentication;
    private Boolean checkAccessService;
    private Boolean checkAccessAccount;
    private Boolean checkAccessWithdraw;
    private String amountProperty;
    private String accountProperty;

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getSystemic() {
        return isSystemic;
    }

    public void setSystemic(Boolean systemic) {
        isSystemic = systemic;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public Service getParent() {
        return parent;
    }

    public void setParent(Service parent) {
        this.parent = parent;
    }

    public ServiceImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(ServiceImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    public String getRequestJsonSchema() {
        return requestJsonSchema;
    }

    public void setRequestJsonSchema(String requestJsonSchema) {
        this.requestJsonSchema = requestJsonSchema;
    }

    public String getResponseJsonSchema() {
        return responseJsonSchema;
    }

    public void setResponseJsonSchema(String responseJsonSchema) {
        this.responseJsonSchema = responseJsonSchema;
    }

    public Boolean getCheckAccessFirstAuthentication() {
        return checkAccessFirstAuthentication;
    }

    public void setCheckAccessFirstAuthentication(Boolean checkAccessFirstAuthentication) {
        this.checkAccessFirstAuthentication = checkAccessFirstAuthentication;
    }

    public Boolean getCheckAccessSecondAuthentication() {
        return checkAccessSecondAuthentication;
    }

    public void setCheckAccessSecondAuthentication(Boolean checkAccessSecondAuthentication) {
        this.checkAccessSecondAuthentication = checkAccessSecondAuthentication;
    }

    public Boolean getCheckAccessService() {
        return checkAccessService;
    }

    public void setCheckAccessService(Boolean checkAccessService) {
        this.checkAccessService = checkAccessService;
    }

    public Boolean getCheckAccessAccount() {
        return checkAccessAccount;
    }

    public void setCheckAccessAccount(Boolean checkAccessAccount) {
        this.checkAccessAccount = checkAccessAccount;
    }

    public Boolean getCheckAccessWithdraw() {
        return checkAccessWithdraw;
    }

    public void setCheckAccessWithdraw(Boolean checkAccessWithdraw) {
        this.checkAccessWithdraw = checkAccessWithdraw;
    }

    public String getAmountProperty() {
        return amountProperty;
    }

    public void setAmountProperty(String amountProperty) {
        this.amountProperty = amountProperty;
    }

    public String getAccountProperty() {
        return accountProperty;
    }

    public void setAccountProperty(String accountProperty) {
        this.accountProperty = accountProperty;
    }

    public abstract Object getServiceInfo();

}
