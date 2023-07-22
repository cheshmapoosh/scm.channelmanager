package ir.daneshrefah.scm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TBL_SCM_SERVICE_COMPONENT_PROVIDER")
public class ServiceComponentProviderEntity extends AbstractEntity {
    @Id
    @Column(name = "SERVICE_COMPONENT_PROVIDER_ID")
    private String id;
    private String code;
    private String title;

    public String getCode() {
        return code;
    }

    public void setCode(String name) {
        this.code = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}
