package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.transformerUtil.CardSystemSecurityUtil
import ir.daneshrefah.scm.common.transformerUtil.constant.CardServiceName
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.utils.string.StringUtils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def body = exchange.in.body
println("rest cardXferAdd rq transformer start")
def card = body.cardNumber
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
def amount = body.amount
if (amount.length() < 12) {
    amount = StringUtils.leftPadZero(String.valueOf(amount), 12)
    println("amount : " + amount)
}
def pin = body.pin
print("pin before encrypt : " + pin)
pin = CardSystemSecurityUtil.encryptPin(pin.toString(), card.toString())
print("pin after encrypt : " + pin)

def req = [:]

req.put("serviceName", CardServiceName.CARD_XFER_ADD)

def data = [:]
data.put("cardNumber", card)
data.put("stan", stan)
data.put("posData", CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA)
data.put("reference", "691199" + stan)
data.put("cvv", body["cvv"])
data.put("expiryDate", body["expiryDate"])
data.put("cardAccTermId", CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
data.put("cardAccId", CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE)
data.put("dateAndTime", dateAndTime)
data.put("cardAccNameAddress", CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)
data.put("destCard", body["destCard"])
data.put("amount", amount)
data.put("pin", pin)
data.put("accountNumber", body.accountNumber)
data.put("transBind", "") //?????????????// name last name az card inquiry


req.put("data", data)

println("rest cardXferAdd rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)