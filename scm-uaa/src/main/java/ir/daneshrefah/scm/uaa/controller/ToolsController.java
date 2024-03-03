package ir.daneshrefah.scm.uaa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.Date;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Controller
@RequestMapping("/public/tools")
public class ToolsController extends BaseController {

    private static final String TEMPLATE_NAME_BASE64 = "base-64";
    private static final String TEMPLATE_NAME_JWT = "jwt";
    private static final String TEMPLATE_NAME_TIME = "time";
    private static final String TEMPLATE_NAME_CONVERTERS = "converters";

    @GetMapping("/")
    public String showToolsPage(Model model) {
        model.addAttribute("encodedValue", "");
        return TEMPLATE_NAME_CONVERTERS;
    }

    @GetMapping("/time")
    public String showTimeDecodePage(Model model) {
        model.addAttribute("encodedValue", "");
        return TEMPLATE_NAME_TIME;
    }

    @PostMapping("/time")
    public String decodeTime(@RequestParam Long encodedValue, Model model) {
        model.addAttribute("encodedValue", encodedValue);
        model.addAttribute("decodedValue", new Date(encodedValue * 1000));
//        model.addAttribute("decodedValue", Instant.ofEpochSecond(encodedValue));
        return TEMPLATE_NAME_TIME;
    }

    @GetMapping("/base64")
    public String showBase64DecodePage(Model model) {
        model.addAttribute("encodedValue", "");
        return TEMPLATE_NAME_BASE64;
    }

    @PostMapping("/base64")
    public String decodeBase64(@RequestParam String encodedValue, Model model) {
        model.addAttribute("encodedValue", encodedValue);
        model.addAttribute("decodedValue", decodeBase64(encodedValue));
        return TEMPLATE_NAME_BASE64;
    }

    @GetMapping("/jwt")
    public String showJwtDecodePage(Model model) {
        model.addAttribute("encodedValue", "");
        return TEMPLATE_NAME_JWT;
    }

    @PostMapping("/jwt")
    public String decodeJwt(@RequestParam String encodedValue, Model model) {
        /*try {
            JWTParser.parse(encodedValue).getJWTClaimsSet();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        String decodedString = null;
        final int firstDotPos = encodedValue.indexOf(".");

        if (firstDotPos == -1) {
            decodedString = "invalid token";
            model.addAttribute("encodedValue", encodedValue);
            model.addAttribute("decodedValue", decodedString);
            return TEMPLATE_NAME_JWT;
        }

        String[] chunks = encodedValue.split("\\.");
        String header = decodeBase64(chunks[0]);
        String payload = decodeBase64(chunks[1]);
        decodedString = "Header: \n"
                .concat(header)
                .concat("\n\n")
                .concat("Payload: \n")
                .concat(payload)
                .concat("\n\n")
                .concat("Sign: \n")
                .concat(chunks[2]);

        model.addAttribute("encodedValue", encodedValue);
        model.addAttribute("decodedValue", decodedString);
        return TEMPLATE_NAME_JWT;
    }

    private String decodeBase64(String value) {
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
