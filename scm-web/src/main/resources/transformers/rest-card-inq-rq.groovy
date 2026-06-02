package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.utils.string.StringUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ir.daneshrefah.scm.common.transformerUtil.constant.CardServiceName

def body = exchange.in.body
println("rest card inquiry rq body : " + body)
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

def req = [:]
println("rest cardInquiry rq transformer start")

req.put("serviceName", CardServiceName.CARD_INQUIRY)

def data = [:]
data.put("cardNumber", body["cardNumber"])
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
data.put("amount", StringUtils.leftPadZero("0", 12))
req.put("data", data)

println("rest cardInquiry rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)