package ir.daneshrefah.scm.plugin.api.model.service.composition;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceCompositionType;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Getter
@Setter
public class CompositionService extends Service {

    private ServiceCompositionType compositionType;
    private List<ServiceRelation> relations;
    private List<Response> responseList;

}
