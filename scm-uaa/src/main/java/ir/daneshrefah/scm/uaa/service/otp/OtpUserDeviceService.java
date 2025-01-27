package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.domain.otp.RegisterDeviceResponse;
import ir.daneshrefah.scm.uaa.exception.OtpSerialLengthException;
import ir.daneshrefah.scm.uaa.exception.PasswordTypeIsNotOtp;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpRegisterDeviceRequest;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpUserDeviceService {

    private final UserService userService;
    private final OtpDeviceService otpDeviceService;


    public RegisterDeviceResponse registerOtpDevice(OtpRegisterDeviceRequest request) {
        ValidationUtils.checkBlankString(request.getNationalCode(), () -> new MissingRequiredInputException("nationalCode"));
        ValidationUtils.checkNull(request.getOtpDeviceType(), () -> new MissingRequiredInputException("otpDeviceType"));
        ValidationUtils.checkBlankString(request.getOtpSerialNo(), () -> new MissingRequiredInputException("OtpSerialNo"));
        ValidationUtils.validateStringLength(request.getOtpSerialNo(), 10, () -> new OtpSerialLengthException("Serial No", "Serial No must contain at least 10 characters"));
        GeneralPersonEntity generalPersonEntity = getPersonByUsername(request.getNationalCode());
        if (generalPersonEntity == null) {
            throw new NoMatchRecordFoundException("nationalCode", request.getNationalCode());
        }
        UserEntity userEntity = getUserEntity(generalPersonEntity);
        boolean validOtpAssignment = hasOTPAuthenticationMethod(userEntity, AuthenticationMethod.OTP);
        if (!validOtpAssignment) {
            throw new PasswordTypeIsNotOtp();
        }
        return otpDeviceService.registerOtpDevice(request, generalPersonEntity, userEntity);
    }

    private GeneralPersonEntity getPersonByUsername(String nationalCode) {
        return userService.findRealPersonByNationalCode(nationalCode);
    }

    private UserEntity getUserEntity(GeneralPersonEntity generalPersonEntity) {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        assert loggedInUser != null;
        String terminalCode = loggedInUser.getTerminalCode();
        return userService.findByPersonIdAndLegacyTerminalCode(generalPersonEntity.getId(), terminalCode).stream().findFirst().orElseThrow(() -> new NoMatchRecordFoundException("userId"));
    }

    public boolean hasOTPAuthenticationMethod(UserEntity userEntity, AuthenticationMethod authenticationMethod) {
        return userEntity.getTransactionAuthenticationMethod().equals(authenticationMethod);
    }
}
