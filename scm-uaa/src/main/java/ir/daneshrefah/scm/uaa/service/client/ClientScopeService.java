package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.mapper.ScopeMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ScopeRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ScopeEntity;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeFindRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientScopeService {

    private static final List<Scope> SCOPE_LIST = new ArrayList<>();
    private final ScopeRepository scopeRepository;

    public static List<Scope> getScopeList() {
        return SCOPE_LIST;
    }

    @PostConstruct
    public void init() {
        reloadCache();
    }

    public void reloadCache() {
        synchronized (SCOPE_LIST) {
            SCOPE_LIST.clear();
            SCOPE_LIST.addAll(ScopeMapper.INSTANCE.toModels(scopeRepository.findAll()));
        }
    }

    @Transactional
    public Scope save(Scope scope) {
        findByCode(scope.getCode()).ifPresent(found -> {
            throw new DuplicatedRecordFoundException("code");
        });
        ScopeEntity entity = ScopeMapper.INSTANCE.toEntity(scope);
        ScopeEntity saved = scopeRepository.save(entity);
        Scope model = ScopeMapper.INSTANCE.toModel(saved);
        reloadCache();
        return model;
    }

    public Optional<Scope> get(Long id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return getScopeList().stream().filter(scope -> scope.getId().equals(id)).findFirst();
    }

    public Optional<Scope> findByCode(String code) {
        if (Objects.isNull(code)) {
            return Optional.empty();
        }
        return getScopeList().stream().filter(scope -> scope.getCode().equals(code)).findFirst();
    }

    @Transactional
    public Scope update(Scope scope) {
        ScopeEntity entity = scopeRepository.findById(scope.getId()).orElseThrow(() -> new NoMatchRecordFoundException("scope"));
        entity.setCode(scope.getCode());
        entity.setTitle(scope.getTitle());
        entity.setLastEditDate(scope.getLastEditDate());
        ScopeEntity updated = scopeRepository.save(entity);
        reloadCache();
        return ScopeMapper.INSTANCE.toModel(updated);
    }

    @Transactional
    public Scope remove(Scope scope) {
        ScopeEntity entity = scopeRepository.findById(scope.getId()).orElseThrow(() -> new NoMatchRecordFoundException("scope"));
        entity.setLastEditDate(scope.getLastEditDate());
        scopeRepository.delete(entity);
        reloadCache();
        return ScopeMapper.INSTANCE.toModel(entity);
    }


    public List<Scope> getList(ScopeFindRequest request) {
        return getScopeList()
                .stream()
                .filter(scope -> Objects.isNull(request) || Objects.isNull(request.getId()) || request.getId().equals(scope.getId()))
                .filter(scope -> Objects.isNull(request) || Objects.isNull(request.getTitle()) || scope.getTitle().isBlank() || scope.getTitle().toLowerCase().contains(request.getTitle().toLowerCase()))
                .filter(scope -> Objects.isNull(request) || Objects.isNull(request.getCode()) || request.getCode().isBlank() || request.getCode().equalsIgnoreCase(scope.getCode()))
                .filter(scope -> Objects.isNull(request) || Objects.isNull(request.getCreator()) || request.getCreator().isBlank() || scope.getCreator().toLowerCase().contains(request.getCreator().toLowerCase()))
                .filter(scope -> Objects.isNull(request) || Objects.isNull(request.getLastEditor()) || request.getLastEditor().isBlank() || scope.getLastEditor().toLowerCase().contains(request.getLastEditor().toLowerCase()))
                .toList();
    }
}
