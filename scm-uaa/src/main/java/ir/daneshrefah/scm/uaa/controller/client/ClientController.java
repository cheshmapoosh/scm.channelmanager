package ir.daneshrefah.scm.uaa.controller.client;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import ir.daneshrefah.scm.uaa.service.client.ClientAuthorizationGrantTypeService;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
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
    private final ClientAuthorizationGrantTypeService clientAuthorizationGrantTypeService;

    //TODO IMPORTANT : THESE API MUST ASSIGN ON CORRESPONDING ROLES

    @PostMapping("/list")
    public ResponseEntity<PagedResponseData<ClientResponse>> findPagedClientList(@RequestBody @Valid @NotNull ClientFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.findPagedClientList(request));
    }

    @GetMapping("/find-one/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable("id") @Valid @NotNull @Numeric Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.getClientResponseById(id).orElseThrow(() -> new NoMatchRecordFoundException("clientId")));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ClientResponse> removeClient(@RequestBody @Valid @NotNull ClientRemoveRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.remove(request.getClientId(), request.getLastEditDate()));
    }

    @PostMapping("/create")
    public ResponseEntity<ClientResponse> createClient(@RequestBody @Valid @NotNull ClientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.create(request));
    }

    @PutMapping("/edit")
    public ResponseEntity<ClientResponse> editClient(@RequestBody @Valid @NotNull ClientEditRequest request) {
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
