import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ResponseCode

import java.time.format.DateTimeFormatter
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.utils.string.StringUtils

def body = exchange.in.body
if (!body instanceof Map) {
    return
}

def mti = body.get("mti")
println("tcp card inq rs mti : " + mti)
if (!mti.toString().trim().equals(MTI.TRANSFER_RESPONSE_COMMAND.getCode())) {
    throw new RuntimeException("tcp card inq rs : mti is null")
}

def fields = body.get("fields")
println("tcp card inq rs fields : " + fields)
if (fields == null) {
    throw new RuntimeException("tcp card inq rs : fields is null")
}

println("tcp card inq rs action code" + fields[ISOField.ACTION_CODE.getPosition().toString()])
if (fields[ISOField.ACTION_CODE.getPosition().toString()] == null || !fields[ISOField.ACTION_CODE.getPosition().toString()].toString().equals(ResponseCode.APPROVED.getCode())) {
    throw new RuntimeException("tcp card inq rs action code : " + fields[ISOField.ACTION_CODE.getPosition()].toString())
}

def balance = fields["54"]
def availableBalance = null
def ledgerBalance = null
def depositableAmount = null

//def unpadZero = { srcStr, pattern ->
//    {
//        if (!srcStr.isEmpty() && !pattern.isEmpty()) {
//            def destStr;
//            for (destStr = srcStr; destStr.length() >= pattern.length() && destStr[0..pattern.length() - 1] == pattern; destStr = destStr[pattern.length()..-1]) {
//            }
//
//            return destStr;
//        } else {
//            return srcStr;
//        }
//    }
//}

def createBalance = {
    if (balance.isEmpty()) {
        return null;
    }
    availableBalance = balance.length() >= 40 ? balance[8..19] : null;
    ledgerBalance = balance.length() >= 40 ? balance[28..39] : null;
    try {
        depositableAmount = StringUtils.unPadZero(availableBalance, "0")
    }
    catch (Exception ex) {
        depositableAmount = Double.valueOf(0)
    }
    try {
        ledgerBalance = StringUtils.unPadZero(ledgerBalance, "0")
    }
    catch (Exception ex) {
        ledgerBalance = Double.valueOf(0);
    }
}

createBalance()

def date = fields[ISOField.LOCAL_TRANSACTION_DATE_TIME.getPosition()].format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def amount = !fields[ISOField.TRANSACTION_AMOUNT.getPosition()].isEmpty() ? StringUtils.unPadZero(fields[ISOField.TRANSACTION_AMOUNT.getPosition()], "0") : null

return [
        "fundTransfer"       : [
                "sourceAccountNumber"  : "?",
                "sourceCardNumber"     : fields[ISOField.PAN.getPosition()],
                "destinationCardNumber": "?",
                "amount"               : amount,
                "customerCount"        : 0,
                "followupCode"         : "?",
                "personName"           : [
                        "firstName": "?",
                        "lastName" : "?"
                ],
                "date"                 : date
        ],
        "balance"            : [
                "ledgerBalance"    : ledgerBalance,
                "depositableAmount": depositableAmount
        ],
        "serverResponseCode" : fields[ISOField.ACTION_CODE.getPosition()],
        "processCode"        : fields[ISOField.PROCESSING_CODE.getPosition()],
        "destinationBankName": "?"
]