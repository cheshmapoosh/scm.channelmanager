package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.controller.BaseController;
import ir.daneshrefah.scm.uaa.service.user.UpdatePasswordRequest;
import ir.daneshrefah.scm.uaa.service.user.UserFindRequest;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @CrossOrigin
    @PostMapping("/list")
    public ResponseEntity<PagedResponseData<User>> findPagedUserList(@RequestBody(required = false) UserFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findPagedUserList(request));
    }

    @PostMapping("/add")
    public ResponseEntity<User> createUser(@RequestBody UserDataRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.createUser(request));
    }

    @PutMapping("/login-password/{userId}")
    public ResponseEntity<Boolean> updateUserLoginStaticPassword(@PathVariable Long userId, @RequestBody UpdatePasswordRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserLoginPassword(userId, request));
    }

    @PutMapping("/transaction-password/{userId}")
    public ResponseEntity<User> updateUserTransactionStaticPassword(@PathVariable Long userId, @RequestBody String password) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserTransactionStaticPass(userId,password));
    }

    @PostMapping("/activate/{userId}")
    public ResponseEntity<Boolean> activateUser(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.activateUser(userId, true));
    }

    @PostMapping("/deactivate/{userId}")
    public ResponseEntity<Boolean> deactivateUser(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.activateUser(userId, false));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long userId) {
        userService.deleteUserByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

}
