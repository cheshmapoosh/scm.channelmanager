package ir.daneshrefah.scm.uaa.controller.activation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/activation")
@CrossOrigin
public class UserActivationController {

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('SCOPE_activation')")
    public ResponseEntity<?> findPagedClientList() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
