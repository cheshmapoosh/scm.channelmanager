package ir.daneshrefah.scm.common.model.service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class DirectServiceImplementation implements ServiceImplementation {

    private List<ServiceRelation> serviceRelations;
    private Integer executionPolicy; // 1: all, 2: any

    @Override
    public void execute() {

    }

    public List<ServiceRelation> getServiceRelations() {
        return serviceRelations;
    }

    public void setServiceRelations(List<ServiceRelation> serviceRelations) {
        this.serviceRelations = serviceRelations;
    }

    public Integer getExecutionPolicy() {
        return executionPolicy;
    }

    public void setExecutionPolicy(Integer executionPolicy) {
        this.executionPolicy = executionPolicy;
    }
}
