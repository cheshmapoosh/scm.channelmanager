package ir.daneshrefah.scm.core.entity.service.rest;

import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceEntity;
import ir.daneshrefah.scm.core.entity.service.RestExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

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
//    private String path;
//    private String httpMethod;
//    private RestContentType requestContentType;
//    private RestContentType responseContentType;
//    private Set<ParameterEntity> queryStringParameters;
//    private Set<ParameterEntity> pathVariableParameters;
//    private Set<ParameterEntity> requestBodyParameters;

}
