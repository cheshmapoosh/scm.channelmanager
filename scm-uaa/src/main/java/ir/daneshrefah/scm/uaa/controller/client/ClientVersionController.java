package ir.daneshrefah.scm.uaa.controller.client;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.service.client.ClientVersionService;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientVersionRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.VersionCreateRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.VersionEditRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.VersionRemoveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/client/version")
@CrossOrigin
public class ClientVersionController {

    private final ClientVersionService clientVersionService;

    //TODO IMPORTANT : THESE API MUST ASSIGN ON CORRESPONDING ROLES

    @PostMapping("/create")
    public ResponseEntity<ClientVersion> createVersion(@RequestBody @Valid @NotNull VersionCreateRequest request) {
        ClientVersion clientVersion = new ClientVersion();
        clientVersion.setClientId(request.getClientId());
        clientVersion.setAppVersion(request.getVersion());
        clientVersion.setSignature(request.getSignature());
        clientVersion.setStatus(request.getStatus());
        clientVersion.setForced(request.isForced());
        return ResponseEntity.status(HttpStatus.OK).body(clientVersionService.save(clientVersion));
    }

    @PutMapping("/edit")
    public ResponseEntity<ClientVersion> editSVersion(@RequestBody @Valid @NotNull VersionEditRequest request) {
        ClientVersion clientVersion = new ClientVersion();
        clientVersion.setAppVersion(request.getVersion());
        clientVersion.setSignature(request.getSignature());
        clientVersion.setStatus(request.getStatus());
        clientVersion.setId(request.getId());
        clientVersion.setForced(request.isForced());
        clientVersion.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientVersionService.update(clientVersion));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ClientVersion> removeVersion(@RequestBody @Valid @NotNull VersionRemoveRequest request) {
        ClientVersion clientVersion = new ClientVersion();
        clientVersion.setId(request.getId());
        clientVersion.setLastEditDate(request.getLastEditDate());
        return ResponseEntity.status(HttpStatus.OK).body(clientVersionService.remove(clientVersion));
    }

    @GetMapping("/find-one/{id}")
    public ResponseEntity<ClientVersion> findOneClientVersion(@PathVariable(name = "id") @Valid @NotNull @Numeric String versionId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientVersionService.getClientVersionById(Long.parseLong(versionId)).orElseThrow(() -> new NoMatchRecordFoundException("versionId")));
    }

    @PostMapping("/client-version-list")
    public ResponseEntity<PagedResponseData<ClientVersion>> findOneClientVersionByClientId(@RequestBody @Valid @NotNull ClientVersionRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new PagedResponseData<>(request, clientVersionService.getClientVersionByClientId(Long.parseLong(request.getClientId()))));
    }
    @GetMapping("/checkAppVersion")
    public ResponseEntity<ClientVersion> getClientVersionByAppVersion(@NotNull @RequestHeader String appVersion, @NotNull @RequestHeader String signature) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientVersionService.findClientVersionByAppVersionAndSignature(appVersion,signature));
    }

}
