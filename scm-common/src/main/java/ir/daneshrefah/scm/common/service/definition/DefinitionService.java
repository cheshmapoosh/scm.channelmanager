package ir.daneshrefah.scm.common.service.definition;

import ir.daneshrefah.scm.common.dto.definition.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.definition.Definition;

import java.util.List;

public interface DefinitionService {

    List<Definition> getAllDefinitions(DefinitionFilterRequest request);
    DefinitionResponse getDefinitionById(String id);
    PagedResponseData<DefinitionResponse> getAllDefinitionsByTypes(DefinitionFilterRequest request);
    DefinitionDetailResponse getAllDefinitionDetailsById(DefinitionDetailFilterRequest request);
    DefinitionResponse createDefinition(DefinitionRequest definition) ;
    DefinitionResponse updateDefinition(DefinitionRequest definition);
    DefinitionResponse createDefinitionDetail(DefinitionDetailRequest request);
    DefinitionResponse updateDefinitionDetail(DefinitionDetailRequest request);
    DefinitionResponse findByName(String name);
}
