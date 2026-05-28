package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.transformerUtil.CardSystemSecurityUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def body = exchange.in.body
println("rest cardXferAdd rq transformer start")
def card = body.cardNumber
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
def amount = body.amount
def padZeroLeft = { str, length ->
    {
        if (str.isEmpty()) {
            return "";
        }
        while (str.length() < length) {
            str = "0" + str;
        }
        return str;
    }
}
if (amount.length() < 12) {
    amount = padZeroLeft(amount + "", 12)
    println("amount : " + amount)
}
def pin = body.pin
print("pin before encrypt : " + pin)
pin = CardSystemSecurityUtil.encryptPin(pin.toString(), card.toString())
print("pin after encrypt : " + pin)

def req = [:]

req.put("serviceName", "transferCardCnp")

def data = [:]
data.put("cardNumber", card)
data.put("stan", stan)
data.put("posData", "61051061314C")
data.put("reference", "691199" + stan)
data.put("cvv", body["cvv"])
data.put("expiryDate", body["expiryDate"])
data.put("cardAccTermId", "67777777")
data.put("cardAccId", "   777777777600")
data.put("dateAndTime", dateAndTime)
data.put("cardAccNameAddress", "Refah Bank            Tehran       THRIR010010157171371502184852851")
data.put("destCard", body["destCard"])
data.put("amount", amount)
data.put("pin", pin)
data.put("accountNumber", body.accountNumber)
data.put("transBind", "             05فاطمه 07چقازردی 00")


req.put("data", data)

println("rest cardXferAdd rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)