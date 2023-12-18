package ir.daneshrefah.scm.uaa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Controller
public class HomeController {

    @GetMapping("/home")
    public String login(Model model) {
//        Employee employee = new Employee();
//        model.addAttribute("employee", employee);
        return "home";
    }
}
