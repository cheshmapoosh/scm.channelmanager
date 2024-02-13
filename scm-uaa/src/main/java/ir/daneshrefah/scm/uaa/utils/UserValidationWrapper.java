package ir.daneshrefah.scm.uaa.utils;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@RequiredArgsConstructor
public class UserValidationWrapper {

    private final User user;

    public boolean containsMobile(String mobileNo) {
        if (null == user || null == user.getPerson()) {
            return false;
        }
        if (StringUtils.isEmpty(mobileNo)) {
            return false;
        }
        GeneralPerson person = user.getPerson();
        return mobileNo.equals(person.getMobile1()) || mobileNo.equals(person.getMobile2()) ||
                mobileNo.equals(person.getMobile3());
    }

}
