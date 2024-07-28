package ir.daneshrefah.scm.uaa.controller.client;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.service.client.ClientFindRequest;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/list")
    public ResponseEntity<PagedResponseData<Client>> findPagedClientList(@RequestBody ClientFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(clientService.findPagedClientList(request));
    }

}
