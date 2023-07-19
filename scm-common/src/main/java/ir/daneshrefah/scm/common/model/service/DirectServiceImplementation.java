package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.model.component.ServiceComponentRelation;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class DirectServiceImplementation implements ServiceImplementation {

    private List<ServiceComponentRelation> serviceComponents;
    private Integer executionPolicy; // 1: all, 2: any

    @Override
    public void execute() {

    }

}
