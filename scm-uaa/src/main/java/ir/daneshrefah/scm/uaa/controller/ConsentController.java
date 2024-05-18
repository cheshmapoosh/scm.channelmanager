package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.access.InvalidInvocationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
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
public class ConsentController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/consent")
    public String consent(Model model, @RequestParam(name = OAuth2ParameterNames.SCOPE, required = true) String scope,
                          @RequestParam(name = OAuth2ParameterNames.CLIENT_ID, required = true) String clientId,
                          @RequestParam(name = OAuth2ParameterNames.STATE, required = true) String state,
                          @RequestParam(name = OAuth2ParameterNames.USER_CODE, required = false) String userCode) {
        Client client = clientService.findByClientId(clientId).orElseThrow(() -> new InvalidInvocationException("clientId"));
        model.addAttribute("scopes", scope.split(" "));
        model.addAttribute(OAuth2ParameterNames.CLIENT_ID, clientId);
        model.addAttribute("client_title", client.getTitle());
        model.addAttribute(OAuth2ParameterNames.STATE, state);
        model.addAttribute(OAuth2ParameterNames.USER_CODE, userCode);
        return "consent";
    }

}
