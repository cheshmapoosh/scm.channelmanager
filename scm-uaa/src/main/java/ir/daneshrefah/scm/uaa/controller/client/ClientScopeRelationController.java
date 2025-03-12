package ir.daneshrefah.scm.uaa.controller.client;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.service.client.ClientScopeRelationService;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeRelationCreateRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeRelationFindRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ScopeRelationRevokeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/client/scope-relation")
@CrossOrigin
public class ClientScopeRelationController {

    private final ClientScopeRelationService clientScopeRelationService;

    //TODO IMPORTANT : THESE API MUST ASSIGN ON CORRESPONDING ROLES

    @PostMapping("/find-by-client")
    public ResponseEntity<PagedResponseData<Scope>> findClientScopeRelation(@RequestBody @Valid @NotNull ScopeRelationFindRequest request) {
        List<Scope> found = clientScopeRelationService.findClientScopes(request.getClientId());
        return ResponseEntity.status(HttpStatus.OK).body(new PagedResponseData<>(request, found));
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignScope(@RequestBody @Valid @NotNull ScopeRelationCreateRequest request) {
        clientScopeRelationService.addScope(request.getClientId(), request.getScopeId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokeScope(@RequestBody @Valid @NotNull ScopeRelationRevokeRequest request) {
        clientScopeRelationService.removeScope(Long.parseLong(request.getClientId()), Long.parseLong(request.getScopeId()));
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
