package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import ir.daneshrefah.scm.uaa.mapper.ClientVersionMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientVersionRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientVersionEntity;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.GeneralPwaOauthException;
import ir.daneshrefah.scm.uaa.service.client.dto.VersionFindRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage.CLIENT_INVALID_APP_VERSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientVersionService {

    private static volatile Map<String, ClientVersion> CLIENT_VERSIONS = Map.of();
    private final ClientVersionRepository clientVersionRepository;
    private final ClientRepository clientRepository;
    private final ClientVersionMapper clientVersionMapper;

    public static Collection<ClientVersion> getClientVersionsList() {
        return CLIENT_VERSIONS.values();
    }

    @PostConstruct
    public void init() {
        reloadCache();
    }

    private void reloadCache() {
        CLIENT_VERSIONS = clientVersionRepository.findAll()
                .stream()
                .map(clientVersionMapper::toModel)
                .collect(Collectors.toUnmodifiableMap(
                        ClientVersion::getAppVersion,
                        Function.identity()
                ));
    }

    public List<ClientVersion> getClientVersionByClientId(Long clientId) {
        return CLIENT_VERSIONS
                .values()
                .stream()
                .filter(cv -> Objects.equals(cv.getClientId(), clientId))
                .toList();
    }

    public Optional<ClientVersion> getClientVersionById(Long id) {
        return CLIENT_VERSIONS
                .values()
                .stream()
                .filter(cv -> Objects.equals(cv.getId(), id)).findFirst();
    }

    public Optional<ClientVersion> getClientVersionByAppVersion(String appVersion) {
        return Optional.ofNullable(CLIENT_VERSIONS.get(appVersion));
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
        clientVersionEntity.setVersion(clientVersion.getAppVersion());
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
        clientVersionEntity.getClient().getVersions().removeIf(versionEntity -> versionEntity.getId().equals(clientVersion.getId()));
        clientVersionEntity.setLastEditDate(clientVersion.getLastEditDate());
        clientVersionRepository.delete(clientVersionEntity);
        reloadCache();
        return clientVersionMapper.toModel(clientVersionEntity);
    }

    public List<ClientVersion> getList(VersionFindRequest request) {
        return getClientVersionsList()
                .stream()
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getId()) || request.getId().equals(version.getId()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getCreator()) || request.getCreator().isBlank() || version.getCreator().toLowerCase().contains(request.getCreator().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getLastEditor()) || request.getLastEditor().isBlank() || version.getLastEditor().toLowerCase().contains(request.getLastEditor().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getVersion()) || version.getAppVersion().isBlank() || version.getAppVersion().toLowerCase().contains(request.getVersion().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getSignature()) || version.getSignature().isBlank() || version.getSignature().toLowerCase().contains(request.getSignature().toLowerCase()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getStatus()) || request.getStatus().equals(version.getStatus()))
                .filter(version -> Objects.isNull(request) || Objects.isNull(request.getIsForced()) || request.getIsForced().equals(version.isForced()))
                .toList();
    }

    public void checkAppSignature(String appVersion, String signature) {
        getClientVersionByAppVersion(appVersion)
                .filter(cv -> Objects.equals(cv.getSignature(), signature))
                .orElseThrow(() -> {
                    log.warn("Invalid app version caught:{}", appVersion);
                    return new GeneralPwaOauthException(CLIENT_INVALID_APP_VERSION);
                });

    }

    public boolean isAppSignatureValid(String appVersion, String signature) {
        return getClientVersionByAppVersion(appVersion)
                .filter(cv -> Objects.equals(cv.getSignature(), signature))
                .map(a -> Boolean.TRUE)
                .orElse(false);
    }

    public ClientVersion findClientVersionByAppVersionAndSignature(String appVersion, String signature) {
        return getClientVersionByAppVersion(appVersion)
                .filter(cv -> Objects.equals(cv.getSignature(), signature))
                .map(clientVersionMapper::toEntity)
                .map(clientVersionMapper::toModel)
                .orElseGet(() -> {
                    ClientVersion cv = new ClientVersion();
                    cv.setClientId(0L);
                    cv.setAppVersion(appVersion);
                    cv.setSignature(signature);
                    cv.setStatus(ClientVersionStatus.INVALID);
                    return cv;
                });
    }

}
