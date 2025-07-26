package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.controller.BaseController;
import ir.daneshrefah.scm.uaa.service.user.UserDeleteRequest;
import ir.daneshrefah.scm.uaa.service.user.UserFindRequest;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
@CrossOrigin
public class UserController extends BaseController {

    private final UserService userService;


    @PutMapping("/change-nickName")
    public ResponseEntity<User> changeUserName(@RequestBody UserNickNameModifyRequest request,HttpServletRequest servletRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.changeNickName(request,servletRequest));
    }

    //TODO IMPORTANT !!! THIS ROLE MUST BE CHANGED !!!!

    @PutMapping("/change-nickName-by-employee")
    public ResponseEntity<User> changeUserNameByAdmin(@RequestBody UserNickNameModifyRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.changeNickNameByAdmin(request));
    }
 
    @PutMapping("/change-login-password")
    public ResponseEntity<User> updateUserLoginStaticPassword(@RequestBody PasswordModificationRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserLoginStaticPassword(request,false));
    }

    @PutMapping("/change-transaction-password")
    public ResponseEntity<?> updateUserTransactionStaticPassword(@RequestBody PasswordModificationRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserTransactionStaticPass(request));
    }

    @PutMapping("/change-login-password-method")
    public ResponseEntity<User> updateLoginPasswordMethod(@RequestBody AuthenticationMethodModificationRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateLoginPasswordMethod(request));
    }

    @PutMapping("/change-transaction-password-method")
    public ResponseEntity<User> updateTransactionPasswordMethod(@RequestBody AuthenticationMethodModificationRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateTransactionPasswordMethod(request,servletRequest));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Boolean> updatePasswordMethod(@RequestBody UpdatePasswordRequest request) { //TODO this method for figital,validate before change it
        return ResponseEntity.status(HttpStatus.OK).body(userService.UpdatePasswordRequest(request));
    }

    @PutMapping("/change-authentication-method")
    public ResponseEntity<User> changeAuthenticationMethod(@RequestBody ChangeAuthenticationMethodRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.changeAuthenticationMethodByNationalCodeAndTerminalId(request));
    }

    @PostMapping("/list")
    public ResponseEntity<PagedResponseData<User>> findPagedUserList(@RequestBody(required = false) UserFindRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findPagedUserList(request));
    }

    @PostMapping("/add")
    public ResponseEntity<User> createUser(@RequestBody UserDataRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.createUser(request));
    }

    @PutMapping("/change") //TODO NEEDS ROLE ACCESS PERMISSION
    public ResponseEntity<User> changeUser(@RequestBody UserDataChangeRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.changeUser(request));
    }


    @PostMapping("/activate/{userId}")
    public ResponseEntity<Boolean> activateUser(@PathVariable("userId") Integer userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.activateUser(userId, true));
    }

    @PostMapping("/deactivate/{userId}")
    public ResponseEntity<Boolean> deactivateUser(@PathVariable("userId") Integer userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.activateUser(userId, false));
    }

    @PostMapping("/change-user-status")
    public ResponseEntity<Boolean> activateOrDeactivateStatusUser(@RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.activateOrDeactivateStatusUser(request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("userId") Integer userId, @RequestBody UserDeleteRequest userDeleteRequest) {
        userService.deleteUserByUserId(userId,userDeleteRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> findUserById(@PathVariable("userId") Integer userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findUserById(userId));
    }

    @PostMapping("/assign-terminal")
    public ResponseEntity<User> assignTerminalToPerson(@RequestBody UserAssignTerminalRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.assignTerminalToPerson(request));
    }
}
