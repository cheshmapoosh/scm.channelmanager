package ir.daneshrefah.scm.uaa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model, @RequestParam(name = "client_id", required = false) String clientId) {
//        Employee employee = new Employee();
//        model.addAttribute("employee", employee);
        return "login";
    }

}
