package ir.daneshrefah.scm.uaa.utils;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
@RequiredArgsConstructor
public class PersonValidationWrapper {

    private final GeneralPerson person;

    public boolean containsMobile(String mobileNo) {
        if (Objects.isNull(person)) {
            return false;
        }
        if (StringUtils.isEmpty(mobileNo)) {
            return false;
        }
        return mobileNo.equals(person.getMobile1()) || mobileNo.equals(person.getMobile2()) ||
                mobileNo.equals(person.getMobile3());
    }

}
