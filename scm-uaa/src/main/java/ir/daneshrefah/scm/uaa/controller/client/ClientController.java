package ir.daneshrefah.scm.uaa.controller.client;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientAuthorizationGrantTypeEntity;
import ir.daneshrefah.scm.uaa.service.client.*;
import ir.daneshrefah.scm.uaa.service.client.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/client")
@CrossOrigin
public class ClientController {

    private final ClientService clientService;
    private final ClientScopeService clientScopeService;
    private final ClientVersionService clientVersionService;
    private final ClientScopeRelationService clientScopeRelationService;
    private final ClientAuthorizationGrantTypeService clientAuthorizationGrantTypeService;

    //TODO IMPORTANT : THESE API MUST ASSIGN ON CORRESPONDING ROLES

    //SCOPE

    @PostMapping("/scope/list")
    public ResponseEntity<PagedResponseData<Scope>> getAllScopesList(@RequestBody @Valid @NotNull ScopeFindRequest request) {
        List<Scope> found = clientScopeService.getList(request);
        return ResponseEntity.status(HttpStatus.OK).body(new PagedResponseData<>(request, found));
    }

    @PostMapping("/scope/create")
    public ResponseEntity<Scope> createScope(@RequestBody @Valid @NotNull ScopeCreateRequest request) {
        Scope scope = new Scope(request.getCode(), request.getTitle());
        return ResponseEntity.status(HttpStatus.OK).body(clientScopeService.save(scope));
    }

    @PostMapping("/scope/edit")
    public ResponseEntity<Scope> editScope(@RequestBody @Valid @NotNull ScopeEditRequest request) {
        Scope scope = new Scope(request.getCode(), request.getTitle());
        scope.setId(request.getId());
        scope.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientScopeService.update(scope));
    }

    @PostMapping("/scope/remove")
    public ResponseEntity<Scope> removeScope(@RequestBody @Valid @NotNull ScopeRemoveRequest request) {
        Scope scope = new Scope();
        scope.setId(request.getId());
        scope.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientScopeService.remove(scope));
    }

    @GetMapping("/scope/find-one/{id}")
    public ResponseEntity<Scope> findOneScopeVersion(@PathVariable(name = "id") @Valid @NotNull @Numeric Long scopeId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientScopeService.get(scopeId).orElseThrow(() -> new NoMatchRecordFoundException("scopeId")));
    }


    // VERSION

    @PostMapping("/version/create")
    public ResponseEntity<ClientVersion> createVersion(@RequestBody @Valid @NotNull VersionCreateRequest request) {
        ClientVersion clientVersion = new ClientVersion();
        clientVersion.setClientId(request.getClientId());
        clientVersion.setVersion(request.getVersion());
        clientVersion.setSignature(request.getSignature());
        clientVersion.setStatus(request.getStatus());
        clientVersion.setForced(request.isForced());
        return ResponseEntity.status(HttpStatus.OK).body(clientVersionService.save(clientVersion));
    }

    @PostMapping("/version/edit")
    public ResponseEntity<ClientVersion> editSVersion(@RequestBody @Valid @NotNull VersionEditRequest request) {
        ClientVersion clientVersion = new ClientVersion();
        clientVersion.setVersion(request.getVersion());
        clientVersion.setSignature(request.getSignature());
        clientVersion.setStatus(request.getStatus());
        clientVersion.setId(request.getId());
        clientVersion.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientVersionService.update(clientVersion));
    }

    @PostMapping("/version/remove")
    public ResponseEntity<ClientVersion> removeVersion(@RequestBody @Valid @NotNull VersionRemoveRequest request) {
        ClientVersion clientVersion = new ClientVersion();
        clientVersion.setId(request.getId());
        clientVersion.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientVersionService.remove(clientVersion));
    }

    @GetMapping("/version/find-one/{id}")
    public ResponseEntity<ClientVersion> findOneClientVersion(@PathVariable(name = "id") @Valid @NotNull @Numeric Long versionId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientVersionService.getClientVersionById(versionId).orElseThrow(() -> new NoMatchRecordFoundException("scopeId")));
    }

    //CLIENT SCOPE RELATION

    @PostMapping("/scope-relation/find-by-client")
    public ResponseEntity<PagedResponseData<Scope>> findClientScopeRelation(@RequestBody @Valid @NotNull ScopeRelationFindRequest request) {
        List<Scope> found = clientScopeRelationService.findClientScopes(request.getClientId());
        return ResponseEntity.status(HttpStatus.OK).body(new PagedResponseData<>(request, found));
    }

    @PostMapping("/scope-relation/assign")
    public ResponseEntity<?> assignScope(@RequestBody @Valid @NotNull ScopeRelationCreateRequest request) {
        clientScopeRelationService.addScope(request.getClientId(), request.getScopeId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/scope-relation/revoke")
    public ResponseEntity<?> revokeScope(@RequestBody @Valid @NotNull ScopeRelationRevokeRequest request) {
        clientScopeRelationService.removeScope(request.getClientId(), request.getScopeId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    // CLIENT

    @PostMapping("/list")
    public ResponseEntity<PagedResponseData<Client>> findPagedClientList(@RequestBody @Valid @NotNull ClientFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.findPagedClientList(request));
    }

    @GetMapping("/find-one/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable("id") @Valid @NotNull @Numeric Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("clientId")));
    }

    @PostMapping("/remove")
    public ResponseEntity<Client> removeClient(@RequestBody @Valid @NotNull ClientRemoveRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.remove(request.getClientId(),request.getLastEditDate()));
    }

    @PostMapping("/create")
    public ResponseEntity<Client> createClient(@RequestBody @Valid @NotNull ClientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.create(request));
    }

    @PostMapping("/edit")
    public ResponseEntity<Client> editClient(@RequestBody @Valid @NotNull ClientEditRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.updateClient(request));
    }

    // CLIENT AUTHENTICATION METHOD

    @GetMapping("/auth-method/list")
    public ResponseEntity<List<ClientAuthenticationMethod>> getAllAuthMethodList() {
        List<ClientAuthenticationMethod> found = Arrays.stream(ClientAuthenticationMethod.values()).toList();
        return ResponseEntity.status(HttpStatus.OK).body(found);
    }

    // CLIENT AUTHENTICATION GRANT TYPES

    @GetMapping("/auth-grant/list")
    public ResponseEntity<List<AuthorizationGrantType>> getAllAuthGrantList() {
        List<AuthorizationGrantType> found = clientAuthorizationGrantTypeService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(found);
    }

}
