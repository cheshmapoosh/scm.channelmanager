package ir.daneshrefah.scm.core.entity.transformer;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_TRANSFORMER")
public class TransformerEntity extends AbstractDefaultEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TRANSFORMER_ID")
    private String id;
    private String title;
    @Column(name = "TRANSFORMER_METADATA")
    private String metadata;
    private String javaClassName;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }
}
