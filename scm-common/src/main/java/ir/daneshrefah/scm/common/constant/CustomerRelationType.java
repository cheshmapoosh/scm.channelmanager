package ir.daneshrefah.scm.common.constant;

import ir.daneshrefah.scm.common.model.asset.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Getter
@RequiredArgsConstructor
public enum CustomerRelationType {

    /*
         Data Provider :
         curl --location --request POST 'https://scm-core.daneshrefah.ir/Service/scmread.GETDETAILSTATUS' \
               --header 'Content-Type: application/json' \
               --data-raw '{
                    "parameters": [
                        {
                            "name": "P_MASTERID",
                            "value": "101"
                        }
                    ],
                    "callType": "Reader",
                    "encoding": "ASCII",
                    "requestID": "RequestID"
                }'
     */

    ACCOUNT_HOLDER          (1),               //صاحب حساب
    ACCOUNT_GUARDIAN        (2),               //ولی حساب
    CUSTODIAN               (3),               //قیم
    EXECUTOR                (4),               //وصی
    ATTORNEY                (5),               //وکیل
    REFERRER                (6),               //معرف
    GUARANTOR               (7),               //ضامن
    TRUSTEE                 (8),               //امین
    MOTHER                  (9),               // مادر
    AUTHORIZED_SIGNATORY    (10),              //صاحب امضا
    CHAIRMAN_OF_THE_BOARD   (21),              //رئیس هیات مدیره
    CEO                     (22),              //مدیرعامل
    BOARD_MEMBER            (23),              //عضو هیات مدیره
    OTHER                   (24);


    private final Integer code;

    public static CustomerRelationType findByCode(Integer code){
       return Arrays.stream(values())
                .filter(customerRelationType -> customerRelationType.getCode().equals( code))
                .findFirst()
                .orElse(OTHER);
    }
}
