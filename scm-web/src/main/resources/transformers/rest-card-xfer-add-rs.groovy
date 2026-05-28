import ir.daneshrefah.scm.common.exception.CardError
import ir.daneshrefah.scm.common.exception.NabError

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