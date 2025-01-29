package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.common.constant.otp.OtpDeviceType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.domain.otp.OTPState;
import ir.daneshrefah.scm.uaa.domain.otp.OtpAssignRequest;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OtpAssignService {

    private final UserService userService;

    public List<GeneralPerson> findOtpRegistration(OtpAssignRequest otpAssignRequest) {
        List<String> otpDeviceTypeValues = Stream.of(OtpDeviceType.values())
                .map(OtpDeviceType::getValue)
                .toList();
        List<GeneralPersonEntity> generalPersonEntities;
        if (otpAssignRequest.getOtpState().equals(OTPState.OTP_NOT_ASSIGNED)) {
            generalPersonEntities = userService.findOtpRegistration(otpAssignRequest.getNationalCode(), otpDeviceTypeValues);
        } else {
            if (StringUtils.isEmpty(otpAssignRequest.getOtpSerialNo())) {
                generalPersonEntities = userService.findByTokenTypesAndNationalCode(otpAssignRequest.getNationalCode(), otpDeviceTypeValues);
            } else {
                generalPersonEntities = userService.findByTokenTypesAndNationalCodeAndOtpSerialNo(otpAssignRequest.getNationalCode(), otpDeviceTypeValues, otpAssignRequest.getOtpSerialNo());
            }
        }
        return generalPersonEntities.stream().map(PersonMapper.INSTANCE::toPerson).toList();
    }
}
