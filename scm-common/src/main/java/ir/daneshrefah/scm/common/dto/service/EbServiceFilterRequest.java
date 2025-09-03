package ir.daneshrefah.scm.common.dto.service;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EbServiceFilterRequest extends PagedRequestData {
    private Integer id;
    private Boolean publish;
    private String name;
    private String code;
    private String abbreviation;
    private ServiceType serviceType;
    private RoutingStrategy routingStrategy;
}
