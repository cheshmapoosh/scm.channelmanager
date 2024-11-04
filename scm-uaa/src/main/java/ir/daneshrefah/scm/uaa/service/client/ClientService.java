package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.mapper.ClientMapper;
import ir.daneshrefah.scm.uaa.mapper.ScopeMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ScopeRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClientService {

    private List<Scope> scopeList = null;
    private List<Client> clientList = null;

    private final ScopeRepository scopeRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;

    public List<Scope> findScopeList() {
        if (null == scopeList) {
            scopeList = ScopeMapper.INSTANCE.toModels(scopeRepository.findAll());
        }
        return scopeList;
    }

    public Scope findScopeByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return findScopeList().stream().filter(scope -> code.equalsIgnoreCase(scope.getCode()))
                .findFirst()
                .orElse(null);
    }

    public List<Client> findAll() {
        if (null == clientList) {
            clientList = ClientMapper.INSTANCE.toModels(clientRepository.findAll());
        }
        return clientList;
    }

    public PagedResponseData<Client> findPagedClientList(ClientFindRequest request) {
        List<Client> clientList = findAll().stream()
                .filter(client -> Objects.isNull(request.getId()) || client.getId().equals(request.getId()))
                .filter(client -> StringUtils.isBlank(request.getClientId()) || client.getClientId().equals(request.getClientId()))
                .filter(client -> StringUtils.isBlank(request.getTerminalCode()) || client.getTerminalCode().equals(request.getTerminalCode()))
                .filter(client -> StringUtils.isBlank(request.getTitle()) || StringUtils.containsIgnoreCase(client.getTitle(), request.getTitle()))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, clientList);
    }

    public Optional<Client> findByClientId(String clientId) {
        return findAll().stream().filter(client -> clientId.equalsIgnoreCase(client.getClientId())).findFirst();
    }

    public Optional<Client> findById(Long id) {
        return findAll().stream().filter(client -> id == client.getId()).findFirst();
    }

    public Client save(Client client) {
        ClientEntity entity = ClientMapper.INSTANCE.toClientIdEntity(client);
        if (Objects.nonNull(entity.getVersions())) {
            entity.getVersions().forEach(clientVersionEntity -> clientVersionEntity.setClient(entity));
        }
        ClientEntity save = clientRepository.save(entity);
        client = ClientMapper.INSTANCE.toModel(save);
        return client;
    }

    public Client updateClient(Client client) {
//        ClientEntity entity = ClientMapper.INSTANCE.toEntity(client);
//        client = ClientMapper.INSTANCE.toModel(clientRepository.save(entity));
        return client;
    }

//    @Cacheable(cacheNames = "CACHE_CLIENT_AUTHORITY", key = "#clientId")
    public Optional<List<String>> loadClientAuthorities(Long clientId) {
        List<RoleEntity> roles = roleRepository.findByClientId(clientId);
        if (null == roles || roles.isEmpty())
            return Optional.empty();
        return Optional.of(roles.stream()
                .map(r -> r.getCode())
                .collect(Collectors.toList()));
    }

}
