package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthorizationGrantType;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientAuthorizationGrantTypeEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientCreateRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientEditRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING, uses = {UserMapper.class})
public interface ClientMapper {

    @Mapping(target = "authenticationMethods", expression = "java(mapClientAuthenticationMethods(entity))")
    @Mapping(target = "clientAuthorizationGrantTypes", source = "authorizationGrantTypes", qualifiedByName = "mapAuthGrantTypesToModel")
    @Mapping(target = "allowIpAddresses", expression = "java(mapAllowIpAddresses(entity))")
    @Mapping(target = "status", source = "status", qualifiedByName = "toClientModelStatus")
    Client toModel(ClientEntity entity);

    @Mapping(target = "allowIpAddresses", expression = "java(mapAllowIpAddressesString(model))")
    @Mapping(target = "authorizationGrantTypes", source = "clientAuthorizationGrantTypes", qualifiedByName = "mapAuthGrantTypesToEntity")
    @Mapping(target = "versions", source = "versions", ignore = true)
    ClientEntity toEntityInternal(Client model);

    @Mapping(target = "client", ignore = true)
    ClientAuthorizationGrantTypeEntity toEntity(ClientAuthorizationGrantType model);

    ClientAuthorizationGrantType toModel(ClientAuthorizationGrantTypeEntity entity);

    @Named("toClientEntity")
    default ClientEntity toEntity(Client client) {
        ClientEntity entity = toEntityInternal(client);
        mapAuthenticationMethodsToClient(client, entity);
        return entity;
    }

    @Named("mapAuthGrantTypesToModel")
    default Set<ClientAuthorizationGrantType> mapAuthGrantTypesToModel(Set<ClientAuthorizationGrantTypeEntity> authorizationGrantTypes) {
        Set<ClientAuthorizationGrantType> set = new HashSet<>();
        if (Objects.nonNull(authorizationGrantTypes)) {
            authorizationGrantTypes.forEach(authorizationGrantType -> {
                set.add(toModel(authorizationGrantType));
            });
        }
        return set;
    }

    @Named("mapAuthGrantTypesToEntity")
    default Set<ClientAuthorizationGrantTypeEntity> mapAuthGrantTypesToEntity(Set<ClientAuthorizationGrantType> clientAuthorizationGrantTypes) {
        Set<ClientAuthorizationGrantTypeEntity> set = new HashSet<>();
        if (Objects.nonNull(clientAuthorizationGrantTypes)) {
            clientAuthorizationGrantTypes.forEach(clientAuthorizationGrantType -> {
                set.add(toEntity(clientAuthorizationGrantType));
            });
        }
        return set;
    }

    default Set<String> mapAllowIpAddresses(ClientEntity entity) {
        if (Objects.nonNull(entity.getAllowIpAddresses())) {
            String[] elements = StringUtils.split(entity.getAllowIpAddresses(), ",;");
            if (ArrayUtils.isNotEmpty(elements)) {
                return Set.of(elements);
            }
        }
        return Collections.emptySet();
    }

    default String mapAllowIpAddressesString(Client model) {
        if (Objects.nonNull(model.getAllowIpAddresses())) {
            return String.join(",", model.getAllowIpAddresses());
        }
        return null;
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

    default void mapAuthenticationMethodsToClient(Client model, ClientEntity entity) {
        List<ClientAuthenticationMethod> list = model.getAuthenticationMethods();
        if (list.contains(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)) {
            entity.setClientAuthenticationMethodSecretBasic(true);
        }
        if (list.contains(ClientAuthenticationMethod.CLIENT_SECRET_POST)) {
            entity.setClientAuthenticationMethodSecretPost(true);
        }
        if (list.contains(ClientAuthenticationMethod.CLIENT_SECRET_JWT)) {
            entity.setClientAuthenticationMethodSecretJwt(true);
        }
        if (list.contains(ClientAuthenticationMethod.PRIVATE_KEY_JWT)) {
            entity.setClientAuthenticationMethodKeyJwt(true);
        }
        if (list.contains(ClientAuthenticationMethod.NONE)) {
            entity.setClientAuthenticationMethodNone(true);
        }
    }

    List<Client> toModels(Iterable<ClientEntity> clientEntities);

    default String mapAllowIpAddresses(Client model) {
        return StringUtils.join(model.getAllowIpAddresses(), ',');
    }


    ClientResponse toResponseInternal(Client entity);

    default ClientResponse toResponse(Client entity) {
        ClientResponse responseInternal = toResponseInternal(entity);
        User user = entity.getUser();
        if (Objects.nonNull(user)) {
            GeneralLegalPerson person = (GeneralLegalPerson) user.getPerson();
            responseInternal.setNickname(user.getNickname());
            if (Objects.nonNull(person)) {
                responseInternal.setTitleFa(person.getTitle());
                responseInternal.setTitle(person.getTitleEnglish());
            }
        }
        return responseInternal;
    }

    @Named("toClientModelStatus")
    default boolean toClientModelStatus(Boolean status) {
        if (Objects.isNull(status)) {
            return false;
        }
        return status;
    }

    default Client toModel(ClientCreateRequest request) {
        Client client = new Client();
        client.setTerminalCode(request.getTerminalCode());
        client.setAuthenticationMethods(request.getAuthenticationMethods());
        client.setJwkSetUri(request.getJwkSetUri());
        client.setTokenAuthenticationSigningAlgorithm(request.getTokenAuthenticationSigningAlgorithm());
        client.setRedirectUris(request.getRedirectUris());
        client.setRequireAuthorizationConsent(request.getRequireAuthorizationConsent());
        client.setRequireProofKey(request.getRequireProofKey());
        client.setCheckVersion(request.getCheckVersion());
        client.setCheckActivation(request.getCheckActivation());
        client.setSessionTimeToLiveMinute(Long.parseLong(request.getSessionTimeToLiveMinute()));
        client.setCheckIpAddress(request.getCheckIpAddress());
        client.setAllowIpAddresses(request.getAllowIpAddresses());
        client.setScopes(null);
        client.setVersions(null);
        return client;
    }

    default Client toModel(ClientEditRequest request) {
        Client model = new Client();
        model.setTerminalCode(request.getTerminalCode());
        model.setAuthenticationMethods(request.getAuthenticationMethods());
        model.setJwkSetUri(request.getJwkSetUri());
        model.setTokenAuthenticationSigningAlgorithm(request.getTokenAuthenticationSigningAlgorithm());
        model.setRedirectUris(request.getRedirectUris());
        model.setRequireAuthorizationConsent(request.getRequireAuthorizationConsent());
        model.setRequireProofKey(request.getRequireProofKey());
        model.setCheckVersion(request.getCheckVersion());
        model.setCheckActivation(request.getCheckActivation());
        model.setSessionTimeToLiveMinute(Long.parseLong(request.getSessionTimeToLiveMinute()));
        model.setCheckIpAddress(request.getCheckIpAddress());
        model.setAllowIpAddresses(request.getAllowIpAddresses());
        model.setScopes(null);
        model.setVersions(null);
        return model;
    }
}
