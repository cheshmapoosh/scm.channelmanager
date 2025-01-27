package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * @version 1.0.0
 */
@RequiredArgsConstructor
@Getter
public enum AccountStatus {

    /*
      Data Provider :
      curl --location --request POST 'https://scm-core.daneshrefah.ir/Service/scmread.GETDETAILSTATUS' \
        --header 'Content-Type: application/json' \
        --data-raw '{
        "parameters": [
           {
            "name": "P_MASTERID",
            "value": "2"
            }
        ],
        "callType": "Reader",
        "encoding": "ASCII",
        "requestID": "RequestID"
        }'
     */

    ACTIVE                  (1,1,"فعال"),
    BANNED_DEPOSIT_FREE     (2,2,"مسدود با قابلیت واریز"),
    STAGNANT                (3,4,"راکد"),
    CLOSED                  (4,8,"بسته"),
    UNCLAIMED               (5,16,"مطالبه نشده");

    private final int code;
    private final int mrnCode;
    private final String title;

    public static Optional<AccountStatus> getAccountStatus(int accountStatusCode) {
        return Arrays.stream(values())
                .filter(status -> status.getCode() == accountStatusCode)
                .findFirst();
    }


}
