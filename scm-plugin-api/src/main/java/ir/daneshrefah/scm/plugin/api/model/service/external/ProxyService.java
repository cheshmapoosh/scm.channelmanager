package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProxyService extends Service {
    private Service targetService;
    private String proxyServiceCode;
    private List<Parameter> parameters;
}
