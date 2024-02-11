package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.mapper.ClientVersionMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientVersionEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientVersionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ClientVersionService {
    private final ClientVersionRepository repository;

    public Optional<ClientVersion> findById(Long id){
        Optional<ClientVersionEntity> byId = repository.findById(id);
        ClientVersion clientVersion= ClientVersionMapper.INSTANCE.toModel(byId.orElse(null));
        return Optional.ofNullable(clientVersion);
    }
}
