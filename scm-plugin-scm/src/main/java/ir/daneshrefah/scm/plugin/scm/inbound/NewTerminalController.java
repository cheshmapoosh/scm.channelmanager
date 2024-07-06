package ir.daneshrefah.scm.plugin.scm.inbound;

import ir.daneshrefah.scm.common.model.message.HttpMessageInput;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractSpringRestInboundController;
import ir.daneshrefah.scm.plugin.api.model.message.DefaultHttpMessageInputBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

//@RestController
@Component
@RequestMapping("/api/reza")
public class NewTerminalController extends AbstractSpringRestInboundController {

    @GetMapping()
    public ResponseEntity<List<String>> getAllProfiles(HttpServletRequest request) {
        HttpMessageInput messageInput = DefaultHttpMessageInputBuilder.builder(request, "ACCOUNT-LIST").build();
        Message message = executeService();
        return ResponseEntity.status(HttpStatus.OK).body(Arrays.asList(new String[] {"hi", "hello"}));
    }

    @PostMapping("/create-terminal")
    public String createTerminal(HttpServletRequest request, @RequestBody Terminal terminal) {
//        Message message = executeService(request, "ACCOUNT-LIST");
        // ...
        return "user-created";  // Return view name
    }

    @RequestMapping()
    public ResponseEntity<List<String>> getAllProfiles2() {
        return ResponseEntity.status(HttpStatus.OK).body(Arrays.asList(new String[] {"hi", "hello"}));
    }

}
