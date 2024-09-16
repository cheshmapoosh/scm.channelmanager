package ir.daneshrefah.scm.common.model.person;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@RequiredArgsConstructor
public enum PersonType {

    UNKNOWN(20,-1), //TODO 'NAB' DOES NOT HAVE TYPE CODE
    REAL(1,50),
    EMPLOYEE(2,-1), //TODO 'NAB' DOES NOT HAVE TYPE CODE
    CORPORATE(3,4),
    GOVERNANCE(4,1),
    BANK(5,2),
    TAMIN(6,3),
    /**
     * this person type is used for clients that defined in {@link ClientPerson}
     * */
    CLIENT(7,-1); //TODO 'NAB' DOES NOT HAVE TYPE CODE

    private final int code;
    /**
     * Provided from : https://scm-core.daneshrefah.ir/Service/scmread.GETDETAILSTATUS
     */
    private final int detailCode;

    public static PersonType findByCode(int code) {
        return Arrays.stream(PersonType.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElse(null);
    }

    public static PersonType findNabDetailCode(int detailCode) {
        return Arrays.stream(PersonType.values())
                .filter(s -> s.detailCode == detailCode)
                .findFirst()
                .orElse(null);
    }

}
