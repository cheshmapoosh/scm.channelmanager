package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.domain.client.ClientScopeRelation;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.mapper.ClientScopeRelationMapper;
import ir.daneshrefah.scm.uaa.mapper.ScopeMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.*;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientScopeRelationEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ScopeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientScopeRelationService {

    private final ClientScopeRelationRepository clientScopeRelationRepository;
    private final ClientRepository clientRepository;
    private final ScopeRepository scopeRepository;

    public List<Scope> findClientScopes(Long clientId) {
        ClientEntity clientEntity = clientRepository.findById(clientId).orElseThrow(() -> new NoMatchRecordFoundException("client"));
        return clientScopeRelationRepository
                .findByClientId(clientEntity.getId())
                .stream()
                .map(ClientScopeRelationEntity::getScope)
                .map(ScopeMapper.INSTANCE::toModel)
                .toList();
    }

    public List<ClientScopeRelation> findClientScopeRelation(Long clientId) {
        ClientEntity clientEntity = clientRepository.findById(clientId).orElseThrow(() -> new NoMatchRecordFoundException("client"));
        return clientScopeRelationRepository
                .findByClientId(clientEntity.getId())
                .stream()
                .map(ClientScopeRelationMapper.INSTANCE::toModel)
                .toList();
    }

    @Transactional
    public void removeScope(Long clientId, Long scopeId) {
        clientScopeRelationRepository
                .findByClientIdAndScopeId(clientId, scopeId)
                .ifPresentOrElse(clientScopeRelationRepository::delete, () -> {
                    throw new NoMatchRecordFoundException("client");
                });
    }

    @Transactional
    public void addScope(Long clientId, Long scopeId) {
        ClientEntity clientEntity = clientRepository.findById(clientId).orElseThrow(() -> new NoMatchRecordFoundException("client"));
        ScopeEntity scopeEntity = scopeRepository.findById(scopeId).orElseThrow(() -> new NoMatchRecordFoundException("scope"));
        ClientScopeRelationEntity clientScopeRelationEntity = new ClientScopeRelationEntity();
        clientScopeRelationEntity.setClient(clientEntity);
        clientScopeRelationEntity.setScope(scopeEntity);
        clientScopeRelationRepository.save(clientScopeRelationEntity);
    }


}
