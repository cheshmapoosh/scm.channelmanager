package ir.daneshrefah.scm.uaa.controller.register;

import ir.daneshrefah.scm.uaa.service.register.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/public/register")
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/submit/{username}")
    public SubmitRegisterResponse submitRegister(@PathVariable String username, @RequestBody SubmitRegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        return registerService.submitUserRegister(username, httpRequest.getRemoteAddr(), request);
    }

    @PostMapping("/confirm/{username}")
    public ConfirmRegisterResponse confirmRegister(@PathVariable String username, @RequestBody ConfirmRegisterRequest request) {
        return registerService.confirmUserRegister(username, request);
    }

}
