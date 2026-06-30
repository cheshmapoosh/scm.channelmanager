package ir.daneshrefah.scm.uaa.controller.message;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.service.client.ClientVersionService;
import ir.daneshrefah.scm.uaa.service.messages.LoginMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@ConditionalOnBean(name = "activationDataSource")
@RequestMapping("/api")
public class LoginMessageController {
    private final ClientVersionService clientService;
    private final LoginMessageService loginMessageService;

    public LoginMessageController(ClientVersionService clientService, LoginMessageService loginMessageService) {
        this.clientService = clientService;
        this.loginMessageService = loginMessageService;
    }

    @PostMapping("/getMessages")
    public ResponseEntity<List<LoginMessageResponseDTO>> getMessages(@RequestBody LoginMessageRequestDTO loginMessageRequestDTO, @RequestHeader("AppVersion") String appVersion) {

        log.info("REST request to get login messages");

        Optional<ClientVersion> clientVersionByAppVersion = clientService.getClientVersionByAppVersion(appVersion);

        return clientVersionByAppVersion
                .map(clientDTO -> {
                    loginMessageRequestDTO.setClientDTO(clientDTO);

                    String applicationCode = clientDTO.getAppVersion();

                    List<LoginMessageResponseDTO> loginMessageResponseDTOList =
                            loginMessageService.findMessages(loginMessageRequestDTO, applicationCode);

                    return ResponseEntity.ok(loginMessageResponseDTOList);
                })
                .orElse(ResponseEntity.notFound().build());


//        return clientService.findByAppVersion(appVersion)
//                .map(clientDTO -> {
//                    loginMessageRequestDTO.setClientDTO(clientDTO);
//
//                    String applicationCode = clientDTO.getApplication();
//
//                    List<LoginMessageResponseDTO> loginMessageResponseDTOList =
//                            loginMessageService.findMessages(loginMessageRequestDTO, applicationCode);
//
//                    return ResponseEntity.ok(loginMessageResponseDTOList);
//                })
//                .orElse(ResponseEntity.notFound().build());
    }
}
