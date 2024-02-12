package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.controller.BaseController;
import ir.daneshrefah.scm.uaa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    private final UserService userService;

    @PostMapping("/paged")
    public PagedResponseData<User> findPagedUserList(@RequestBody(required = false) UserFindRequest request) {
        return userService.findPagedUserList(request);
    }

    @PostMapping
    public User createUser(@RequestBody UserDataRequest request) {
        return userService.createUser(request);
    }

}
