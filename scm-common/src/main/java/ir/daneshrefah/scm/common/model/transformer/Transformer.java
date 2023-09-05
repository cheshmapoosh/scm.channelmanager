package ir.daneshrefah.scm.common.model.transformer;

import ir.daneshrefah.scm.common.BaseModel;

public class Transformer extends BaseModel<String> {

    private String title;
    private String metadata;
    private String javaClassName;

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
