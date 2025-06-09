package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthorizationGrantType;
import ir.daneshrefah.scm.uaa.mapper.ClientMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientAuthorizationGrantTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientAuthorizationGrantTypeService {

    private final ClientAuthorizationGrantTypeRepository authGrantTypeRepository;
    private final ClientMapper mapper;

    public Set<ClientAuthorizationGrantType> findByClientId(Long clientId) {
        return authGrantTypeRepository
                .findByClientId(clientId)
                .stream()
                .map(mapper::toModel)
                .collect(Collectors.toSet());
    }

    public List<AuthorizationGrantType> getAll() {
        return Arrays.stream(AuthorizationGrantType.values()).toList();
    }


}
