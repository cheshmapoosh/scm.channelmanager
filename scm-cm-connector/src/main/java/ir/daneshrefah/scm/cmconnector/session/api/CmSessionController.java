package ir.daneshrefah.scm.cmconnector.session.api;

import ir.daneshrefah.scm.cmconnector.session.model.CmSessionResponse;
import ir.daneshrefah.scm.cmconnector.session.service.CmSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/cm/v1/session")
@RequiredArgsConstructor
public class CmSessionController {
    private final CmSessionService sessionService;

    @GetMapping
    public ResponseEntity<CmSessionResponse> currentSession() {
        return ResponseEntity.ok(sessionService.currentSession());
    }
}
