import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField

def body = exchange.in.body
println("rest cardXferAdd rs transformer start provider body : " + body)

def errorCode = body.errorCode
def out = body.outData

if(out == null && errorCode != null){
    throw new RuntimeException(body.get("errorDescription"))
}

return [
        "actionCode": "",
        "iban":out['iban'],
        "nationalId" :out['nationalId'],
        "reference" :out['reference'],
        "branchCode" :out['branchCode'],
        "dateAndTime" :out['dateAndTime'],
        "destCard" :out['destCard']
]

//return [
//        "fundTransfer"       : [
//                "sourceAccountNumber"  : "?",
//                "sourceCardNumber"     : fields[ISOField.PAN.getPosition()],
//                "destinationCardNumber": "?",
//                "amount"               : amount,
//                "customerCount"        : 0,
//                "followupCode"         : "?",
//                "personName"           : [
//                        "firstName": "?",
//                        "lastName" : "?"
//                ],
//                "date"                 : date
//        ],
//        "balance"            : [
//                "ledgerBalance"    : ledgerBalance,
//                "depositableAmount": depositableAmount
//        ],
//        "serverResponseCode" : fields[ISOField.ACTION_CODE.getPosition()],
//        "processCode"        : fields[ISOField.PROCESSING_CODE.getPosition()],
//        "destinationBankName": "?"
//]