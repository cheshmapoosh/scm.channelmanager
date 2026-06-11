package transformers

import groovy.json.JsonOutput

import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.utils.string.StringUtils
import org.json.simple.JSONObject

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ir.daneshrefah.scm.common.transformerUtil.constant.CardServiceName

def body = exchange.in.body
def fundTransfer = body.fundTransfer
def trk2EquivData = body.trk2EquivData
println("rest card inquiry rq body : " + body)
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

def srcCard = fundTransfer.sourceCardNumber
def destCard = fundTransfer.destinationCardNumber
def pin = trk2EquivData == null ? null : trk2EquivData.pin
def expiryDate = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth
def cvv =  trk2EquivData == null ? null : trk2EquivData.cvv2
println("cvv2 rest card inq : "+ cvv)

def req = [:]
def data = [:]
println("rest cardInquiry rq transformer start")

//JSONObject request = new JSONObject();
//request.put("cvv2", body["cvv"]);
//request.put("expiryDate", body["expiryDate"]);
//request.put("pin", body["pin"]);
//JSONObject decryptedData = CardSystemSecurityUtil.decrypt(request);

req.put("serviceName", CardServiceName.CARD_INQUIRY)

data.put("cardNumber", srcCard)
data.put("stan", stan)
data.put("posData", CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA)
data.put("reference", "691199" + stan)
data.put("cvv", cvv)
data.put("expiryDate", expiryDate)
data.put("cardAccTermId", CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
data.put("cardAccId", CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE)
data.put("dateAndTime", dateAndTime)
data.put("cardAccNameAddress", CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)
data.put("destCard", destCard)
data.put("amount", StringUtils.leftPadZero("0", 12))
req.put("data", data)

println("rest cardInquiry rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)