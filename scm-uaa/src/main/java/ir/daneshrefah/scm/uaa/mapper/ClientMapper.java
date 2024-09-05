package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientAuthorizationGrantTypeEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientScopeRelation;
import jakarta.persistence.Column;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Mapper
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "authenticationMethods", expression = "java(mapClientAuthenticationMethods(entity))")
    @Mapping(target = "authorizationGrantTypes", expression = "java(mapAuthorizationGrantTypes(entity))")
    Client toModel(ClientEntity entity);

    default List<AuthorizationGrantType> mapAuthorizationGrantTypes(ClientEntity entity) {
        List<AuthorizationGrantType> list = new ArrayList<>();
        for (Iterator<ClientAuthorizationGrantTypeEntity> iterator = entity.getAuthorizationGrantTypes().iterator(); iterator.hasNext(); ) {
            ClientAuthorizationGrantTypeEntity authorizationGrantTypeEntity = iterator.next();
            list.add(authorizationGrantTypeEntity.getAuthorizationGrantType());
        }

        return list;
    }

    default List<ClientAuthenticationMethod> mapClientAuthenticationMethods(ClientEntity entity) {
        List<ClientAuthenticationMethod> list = new ArrayList<>();
        if (entity.isClientAuthenticationMethodSecretBasic()) {
            list.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        }
        if (entity.isClientAuthenticationMethodSecretPost()) {
            list.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
        }
        if (entity.isClientAuthenticationMethodSecretJwt()) {
            list.add(ClientAuthenticationMethod.CLIENT_SECRET_JWT);
        }
        if (entity.isClientAuthenticationMethodKeyJwt()) {
            list.add(ClientAuthenticationMethod.PRIVATE_KEY_JWT);
        }
        if (entity.isClientAuthenticationMethodNone()) {
            list.add(ClientAuthenticationMethod.NONE);
        }
        return list;
    }

    /*@Mapping(target = "clientAuthenticationMethodSecretBasic", expression = "java(isClientAuthenticationMethodExist(model, ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod.CLIENT_SECRET_BASIC))")
    @Mapping(target = "clientAuthenticationMethodSecretPost", expression = "java(isClientAuthenticationMethodExist(model, ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod.CLIENT_SECRET_POST))")
    @Mapping(target = "clientAuthenticationMethodSecretJwt", expression = "java(isClientAuthenticationMethodExist(model, ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod.CLIENT_SECRET_JWT))")
    @Mapping(target = "clientAuthenticationMethodKeyJwt", expression = "java(isClientAuthenticationMethodExist(model, ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod.PRIVATE_KEY_JWT))")
    @Mapping(target = "authorizationGrantTypeAuthorizationCode", expression = "java(isAuthorizationGrantTypeExist(model, ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType.AUTHORIZATION_CODE))")
    @Mapping(target = "authorizationGrantTypeRefreshToken", expression = "java(isAuthorizationGrantTypeExist(model, ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType.REFRESH_TOKEN))")
    @Mapping(target = "authorizationGrantTypeClientCredential", expression = "java(isAuthorizationGrantTypeExist(model, ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType.CLIENT_CREDENTIALS))")
    @Mapping(target = "authorizationGrantTypeFirstPassword", expression = "java(isAuthorizationGrantTypeExist(model, ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType.FIRST_PASSWORD))")
    @Mapping(target = "authorizationGrantTypeSecondPassword", expression = "java(isAuthorizationGrantTypeExist(model, ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType.SECOND_PASSWORD))")
    @Mapping(source = "scopes", target = "scopes", qualifiedByName = "toScopeEntities")
    ClientEntity toEntity(Client model);*/

    default boolean isClientAuthenticationMethodExist(Client model, ClientAuthenticationMethod clientAuthenticationMethod) {
        return null != model && null != model.getAuthenticationMethods() &&
                model.getAuthenticationMethods().contains(clientAuthenticationMethod);
    }

    default boolean isAuthorizationGrantTypeExist(Client model, AuthorizationGrantType grantType) {
        return null != model && null != model.getAuthenticationMethods() &&
                model.getAuthorizationGrantTypes().contains(grantType);
    }

    List<Client> toModels(Iterable<ClientEntity> clientEntities);

    ClientEntity toClientIdEntity(Client model);

//    @Mapping(source = "client", target = "client", qualifiedByName = "toClientIdEntity")
//    ClientScopeRelation toScopeEntity(ir.daneshrefah.scm.uaa.domain.client.ClientScopeRelation model);

    List<ClientScopeRelation> toScopeEntities(Iterable<ir.daneshrefah.scm.uaa.domain.client.ClientScopeRelation> entities);

}
