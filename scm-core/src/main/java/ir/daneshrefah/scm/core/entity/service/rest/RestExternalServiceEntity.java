package ir.daneshrefah.scm.core.entity.service.rest;

import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceEntity;
import ir.daneshrefah.scm.core.entity.service.RestExternalServiceProviderEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("6")
public class RestExternalServiceEntity extends AbstractExternalServiceEntity<RestExternalServiceProviderEntity> {

//    TODO dariush complete rest model and use in DefaultRestServiceProviderExecutor
    private String path;
    private HttpMethod httpMethod;
    private HttpContentType requestContentType;
//    private RestContentType responseContentType;
//    private Set<ParameterEntity> queryStringParameters;
//    private Set<ParameterEntity> pathVariableParameters;
//    private Set<ParameterEntity> requestBodyParameters;

}
