package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.core.converter.ServiceImplementationTypeConverter;
import ir.daneshrefah.scm.core.converter.ServiceTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_SERVICE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "SERVICE_IMPLEMENTATION_TYPE_CODE", discriminatorType = DiscriminatorType.INTEGER)
public abstract class ServiceEntity extends AbstractEntity<String> {

    @Id
    @Column(name = "SERVICE_ID")
    private String id;
    private String code;
    private String title;
    private String alias;
    private Integer version;
    private Boolean isSystemic;
    private String metadata;
    @Column(name = "SERVICE_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceTypeConverter.class)
    private ServiceType type;
    @Column(name = "SERVICE_IMPLEMENTATION_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceImplementationTypeConverter.class)
    private ServiceImplementationType implementationType;
    @Column(name = "REQUEST_JSON_SCHEMA", nullable = true)
    private String requestJsonSchema;
    @Column(name = "RESPONSE_JSON_SCHEMA", nullable = true)
    private String responseJsonSchema;
    private Boolean checkAccessFirstAuthentication;
    private Boolean checkAccessSecondAuthentication;
    private Boolean checkAccessService;
    private Boolean checkAccessAccount;
    private Boolean checkAccessWithdraw;
    @Column(name = "PROPERTY_NAME_AMOUNT")
    private String amountProperty;
    @Column(name = "PROPERTY_NAME_ACCOUNT")
    private String accountProperty;
    @ManyToOne
    @JoinColumn(name = "PARENT_SERVICE_ID")
    private ServiceEntity parent;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
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

    public ServiceEntity getParent() {
        return parent;
    }

    public void setParent(ServiceEntity parent) {
        this.parent = parent;
    }
}
