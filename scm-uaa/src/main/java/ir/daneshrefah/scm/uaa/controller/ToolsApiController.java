package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-03
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/public/api/tools")
public class ToolsApiController extends BaseController {

    private final ClientService clientService;

    @GetMapping(value = "/clients", produces = HTTP_HEADER_CONTENT_TYPE_JSON)
    public ResponseEntity<List<Client>> listClient() {
        return ResponseEntity.ok( clientService.findAll());
    }

    @PostMapping("/base64")
    public ResponseEntity<String> decodeBase64(@RequestBody String encodedValue) {
        return ResponseEntity.ok(decodeBase64Internal(encodedValue));
    }

    @PostMapping("/time")
    public ResponseEntity<String> decodeTime(@RequestBody String encodedValue) {
        return ResponseEntity.ok(new Date(Long.valueOf(encodedValue) * 1000).toString());
    }

    @PostMapping("/jwt")
    public ResponseEntity<String> convertJwt(@RequestBody String encodedValue) {
        String decodedString = null;
        final int firstDotPos = encodedValue.indexOf(".");

        if (firstDotPos == -1) {
            decodedString = "invalid token";
            return ResponseEntity.ok(decodedString);
        }

        String[] chunks = encodedValue.split("\\.");
        String header = decodeBase64Internal(chunks[0]);
        String payload = decodeBase64Internal(chunks[1]);
        decodedString = "Header: \n"
                .concat(header)
                .concat("\n\n")
                .concat("Payload: \n")
                .concat(payload)
                .concat("\n\n")
                .concat("Sign: \n")
                .concat(chunks[2]);

        return ResponseEntity.ok(decodedString);
    }

    private String decodeBase64Internal(String value) {
        String decodedValue = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(value);
            decodedValue = new String(decodedBytes);
        } catch (Exception e) {
            decodedValue = e.getMessage();
        }
        return decodedValue;
    }

}
