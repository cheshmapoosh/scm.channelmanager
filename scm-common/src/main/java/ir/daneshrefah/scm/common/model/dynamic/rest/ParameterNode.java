package ir.daneshrefah.scm.common.model.dynamic.rest;

import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ParameterNode {
    private String name;
    private Parameter value;
    private List<ParameterNode> nextNodes;
}