package ir.daneshrefah.scm.plugin.api.model.service.java;

import ir.daneshrefah.scm.plugin.api.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public class JavaService extends Service {

    private String javaImplementationClassName;

    public String getJavaImplementationClassName() {
        return javaImplementationClassName;
    }

    public void setJavaImplementationClassName(String javaImplementationClassName) {
        this.javaImplementationClassName = javaImplementationClassName;
    }

}
