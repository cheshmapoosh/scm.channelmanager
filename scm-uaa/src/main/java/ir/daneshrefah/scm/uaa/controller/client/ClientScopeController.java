package ir.daneshrefah.scm.uaa.controller.client;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.service.client.ClientScopeService;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeCreateRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeEditRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeFindRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeRemoveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/client/scope")
@CrossOrigin
public class ClientScopeController {

    private final ClientScopeService clientScopeService;

    //TODO IMPORTANT : THESE API MUST ASSIGN ON CORRESPONDING ROLES


    @PostMapping("/list")
    public ResponseEntity<PagedResponseData<Scope>> getAllScopesList(@RequestBody @Valid @NotNull ScopeFindRequest request) {
        List<Scope> found = clientScopeService.getList(request);
        return ResponseEntity.status(HttpStatus.OK).body(new PagedResponseData<>(request, found));
    }

    @PostMapping("/create")
    public ResponseEntity<Scope> createScope(@RequestBody @Valid @NotNull ScopeCreateRequest request) {
        Scope scope = new Scope(request.getCode(), request.getTitle());
        return ResponseEntity.status(HttpStatus.OK).body(clientScopeService.save(scope));
    }

    @PutMapping("/edit")
    public ResponseEntity<Scope> editScope(@RequestBody @Valid @NotNull ScopeEditRequest request) {
        Scope scope = new Scope(request.getCode(), request.getTitle());
        scope.setId(request.getId());
        scope.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientScopeService.update(scope));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Scope> removeScope(@RequestBody @Valid @NotNull ScopeRemoveRequest request) {
        Scope scope = new Scope();
        scope.setId(request.getId());
        scope.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientScopeService.remove(scope));
    }

    @GetMapping("/find-one/{id}")
    public ResponseEntity<Scope> findOneScopeVersion(@PathVariable(name = "id") @Valid @NotNull @Numeric Long scopeId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientScopeService.get(scopeId).orElseThrow(() -> new NoMatchRecordFoundException("scopeId")));
    }


}
