package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow;

import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.service.shahkar.ShahkarOwnershipService;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

@Service
@RequiredArgsConstructor
public class ShahkarOwnershipVerificationService {
    private final ShahkarOwnershipService ownershipService;

    public void verify(String nationalCode, String mobileNumber) {
        ShahkarStatus status = ownershipService.checkOwnership(nationalCode, mobileNumber);
        if (!ShahkarStatus.OWNED.equals(status)) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.SHAHKAR_ERROR);
        }
    }
}
