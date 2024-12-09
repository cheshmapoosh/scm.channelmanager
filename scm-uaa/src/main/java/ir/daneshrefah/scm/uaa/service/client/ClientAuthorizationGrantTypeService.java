package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientAuthorizationGrantTypeEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientAuthorizationGrantTypeRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientAuthorizationGrantTypeService {

    private final ClientAuthorizationGrantTypeRepository authGrantTypeRepository;
    private final ClientRepository clientRepository;

    public List<AuthorizationGrantType> findByClientId(Long clientId) {
        return authGrantTypeRepository
                .findByClientId(clientId)
                .stream()
                .map(ClientAuthorizationGrantTypeEntity::getAuthorizationGrantType)
                .toList();
    }

    public List<AuthorizationGrantType> getAll() {
        return Arrays.stream(AuthorizationGrantType.values()).toList();
    }

    public void assignGrantType(AuthorizationGrantType authorizationGrantType, Long clientId) {
        findByClientId(clientId).stream().filter(grantType -> grantType.equals(authorizationGrantType)).findFirst()
                .ifPresent(grantType -> {
                    throw new DuplicatedRecordFoundException("authorizationGrantType");
                });
        ClientEntity clientEntity = clientRepository.findById(clientId).orElseThrow(() -> new InvalidInputException("clientId"));
        ClientAuthorizationGrantTypeEntity entity = new ClientAuthorizationGrantTypeEntity();
        entity.setAuthorizationGrantType(authorizationGrantType);
        entity.setClient(clientEntity);
        authGrantTypeRepository.save(entity);
    }

    public void revokeGrantType(AuthorizationGrantType authorizationGrantType, Long clientId) {
        ClientEntity clientEntity = clientRepository.findById(clientId).orElseThrow(() -> new InvalidInputException("clientId"));
        authGrantTypeRepository.findByClientIdAndAuthorizationGrantType(clientEntity.getId(), authorizationGrantType).ifPresentOrElse(found -> {
            found.setClient(null);
            ClientAuthorizationGrantTypeEntity merged = authGrantTypeRepository.save(found);
            authGrantTypeRepository.delete(merged);
        }, () -> {
            throw new NoMatchRecordFoundException("clientId");
        });

    }


}
