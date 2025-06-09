package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.domain.client.ClientScopeRelation;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.mapper.ClientScopeRelationMapper;
import ir.daneshrefah.scm.uaa.mapper.ScopeMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientScopeRelationRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ScopeRepository;
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
    private final ScopeMapper scopeMapper;
    private final ClientScopeRelationMapper clientScopeRelationMapper;

    public List<Scope> findClientScopes(Long clientId) {
        ClientEntity clientEntity = clientRepository.findById(clientId).orElseThrow(() -> new NoMatchRecordFoundException("client"));
        return clientScopeRelationRepository
                .findByClientId(clientEntity.getId())
                .stream()
                .map(ClientScopeRelationEntity::getScope)
                .map(scopeMapper::toModel)
                .toList();
    }

    public List<ClientScopeRelation> findClientScopeRelation(Long clientId) {
        ClientEntity clientEntity = clientRepository.findById(clientId).orElseThrow(() -> new NoMatchRecordFoundException("client"));
        return clientScopeRelationRepository
                .findByClientId(clientEntity.getId())
                .stream()
                .map(clientScopeRelationMapper::toModel)
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
        clientScopeRelationRepository.findByClientIdAndScopeId(clientId, scopeId).ifPresent((o) -> {
            throw new DuplicatedRecordFoundException("scope");
        });
        ClientScopeRelationEntity clientScopeRelationEntity = new ClientScopeRelationEntity();
        clientScopeRelationEntity.setClient(clientEntity);
        clientScopeRelationEntity.setScope(scopeEntity);
        clientScopeRelationRepository.save(clientScopeRelationEntity);
    }


}
