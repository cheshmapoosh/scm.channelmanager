package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.service.converter.ConverterService;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.ServiceCode.SVC_CONVERTERS_LIST;

@Service
public class ConverterManagementService extends AbstractJavaService {

    private final ConverterService converterService;

    public ConverterManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ConverterService converterService) {
        super(producerTemplate, objectMapper);
        this.converterService = converterService;
    }

    @JavaService(serviceCode = SVC_CONVERTERS_LIST)
    public List<ConverterService.Converters> getAll(){
        return converterService.getAll();
    }
}
