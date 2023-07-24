package ir.daneshrefah.scm.core.entity.component;

import ir.daneshrefah.scm.core.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TBL_SCM_SERVICE_COMPONENT_PROVIDER")
public class ServiceComponentProviderEntity extends AbstractEntity<String> {
    @Id
    @Column(name = "SERVICE_COMPONENT_PROVIDER_ID")
    private String id;
    private String code;
    private String title;
    private String componentClassName;
    private String metadata;

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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComponentClassName() {
        return componentClassName;
    }

    public void setComponentClassName(String componentClassName) {
        this.componentClassName = componentClassName;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

}
