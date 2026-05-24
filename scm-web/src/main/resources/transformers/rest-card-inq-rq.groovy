package transformers

import groovy.json.JsonOutput

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def body = exchange.in.body
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

def req = [:]
println("rest cardInquiry rq transformer start")

req.put("serviceName", "getCardholderNameCnp")

def data = [:]
data.put("cardNumber", body["cardNumber"])
data.put("stan", stan)
data.put("posData", "61051061314C")
data.put("reference", "691199" + stan)
data.put("cvv", body["cvv"])
data.put("expiryDate", body["expiryDate"])
data.put("cardAccTermId", "67777777")
data.put("cardAccId", "777777777600")
data.put("dateAndTime", dateAndTime)
data.put("cardAccNameAddress", "Refah Bank            Tehran       THRIR010010157171371502184852851")
data.put("destCard", body["destCard"])
data.put("amount", "000000000000")
req.put("data", data)

println("rest cardInquiry rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)