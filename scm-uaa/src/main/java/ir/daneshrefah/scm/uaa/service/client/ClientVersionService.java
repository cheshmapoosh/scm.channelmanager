package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.mapper.ClientVersionMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientVersionRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientVersionEntity;
import ir.daneshrefah.scm.uaa.service.client.dto.VersionFindRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientVersionService {

    private final ClientVersionRepository clientVersionRepository;
    private final ClientRepository clientRepository;
    private static final List<ClientVersion> CLIENT_VERSIONS = new ArrayList<>();
    private final ClientVersionMapper clientVersionMapper;

    @PostConstruct
    public void init() {
        reloadCache();
    }

    private void reloadCache() {
        synchronized (CLIENT_VERSIONS) {
            CLIENT_VERSIONS.clear();
            clientVersionRepository.flush();
            clientVersionRepository
                    .findAll()
                    .stream()
                    .map(clientVersionMapper::toModel)
                    .forEach(CLIENT_VERSIONS::add);
        }
    }

    public List<ClientVersion> getClientVersionByClientId(Long clientId) {
        return CLIENT_VERSIONS
                .stream()
                .filter(clientVersion -> Objects.equals(clientVersion.getClientId(),clientId))
                .toList();
    }

    public Optional<ClientVersion> getClientVersionById(Long id) {
        return CLIENT_VERSIONS
                .stream()
                .filter(clientVersion -> clientVersion.getId().equals(id))
                .findFirst();
    }

    public ClientVersion save(ClientVersion clientVersion) {
        ClientEntity clientEntity = clientRepository.findById(clientVersion.getClientId()).orElseThrow(() -> new InvalidInputException("clientId"));
        ClientVersionEntity entity = clientVersionMapper.toEntity(clientVersion);
        entity.setClient(clientEntity);
        ClientVersionEntity saved = clientVersionRepository.save(entity);
        reloadCache();
        return clientVersionMapper.toModel(saved);
    }

    public ClientVersion update(ClientVersion clientVersion) {
        ClientVersionEntity clientVersionEntity = clientVersionRepository.findById(clientVersion.getId()).orElseThrow(() -> new InvalidInputException("id"));
        clientVersionEntity.setVersion(clientVersion.getVersion());
        clientVersionEntity.setSignature(clientVersion.getSignature());
        clientVersionEntity.setStatus(clientVersion.getStatus());
        clientVersionEntity.setForced(clientVersion.isForced());
        clientVersionEntity.setLastEditDate(clientVersion.getLastEditDate());
        ClientVersionEntity updated = clientVersionRepository.save(clientVersionEntity);
        reloadCache();
        return clientVersionMapper.toModel(updated);
    }

    public ClientVersion remove(ClientVersion clientVersion) {
        ClientVersionEntity clientVersionEntity = clientVersionRepository.findById(clientVersion.getId()).orElseThrow(() -> new InvalidInputException("clientVersionId"));
        clientVersionEntity.getClient().getVersions().removeIf(versionEntity-> versionEntity.getId().equals(clientVersion.getId()));
        clientVersionEntity.setLastEditDate(clientVersion.getLastEditDate());
        clientVersionRepository.delete(clientVersionEntity);
        reloadCache();
        return clientVersionMapper.toModel(clientVersionEntity);
    }

    public static List<ClientVersion> getClientVersionsList() {
        return CLIENT_VERSIONS;
    }

    public List<ClientVersion> getList(VersionFindRequest request) {
        return getClientVersionsList()
                .stream()
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getId()) || request.getId().equals(version.getId()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getCreator()) || request.getCreator().isBlank() || version.getCreator().toLowerCase().contains(request.getCreator().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getLastEditor()) || request.getLastEditor().isBlank() || version.getLastEditor().toLowerCase().contains(request.getLastEditor().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getVersion()) || version.getVersion().isBlank() || version.getVersion().toLowerCase().contains(request.getVersion().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getSignature()) || version.getSignature().isBlank() || version.getSignature().toLowerCase().contains(request.getSignature().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getStatus()) || request.getStatus().equals(version.getStatus()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getIsForced()) || request.getIsForced().equals(version.isForced()))
                .toList();
    }
}
