package ir.daneshrefah.scm.gateway.controller;

import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-15
 */
@RestController
@RequestMapping("/terminal")
public class TerminalController extends AbstractController {

    @Autowired
    private TerminalService terminalService;

    @GetMapping
    public List<Terminal> getTerminalList() {
//        return "Hello from Spring MVC Controller!";
        return terminalService.findAllTerminals();
    }

}
