package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.ResponseData;
import ir.daneshrefah.scm.common.model.dynamic.rest.ParameterNode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParameterTreeFindResponse implements ResponseData {
    private ParameterNode tree;
}
