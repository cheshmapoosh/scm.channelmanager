package ir.daneshrefah.scm.core.entity.service;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Entity
@DiscriminatorValue("2")
public class JavaServiceEntity extends ServiceEntity {

    @Column(name = "IMPLEMENTATION_JAVA_CLASS_NAME")
    private String javaImplementationClassName;

    public String getJavaImplementationClassName() {
        return javaImplementationClassName;
    }

    public void setJavaImplementationClassName(String javaImplementationClassName) {
        this.javaImplementationClassName = javaImplementationClassName;
    }
}
