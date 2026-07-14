package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.constant.CacheConstants
import ir.daneshrefah.scm.common.transformerUtil.constant.CardServiceName
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.utils.string.StringUtils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def body = exchange.in.body
def header = exchange.in.headers
def fundTransfer = body.fundTransfer
if (fundTransfer == null) {
    throw new RuntimeException("fundTransfer is Empty")
}
def trk2EquivData = body.trk2EquivData
def card = fundTransfer.sourceCardNumber
def destCardNo = fundTransfer.destinationCardNumber
def amount = fundTransfer.amount
def srcAccount = fundTransfer.sourceAccountNumber

def expiryDate = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth
def pin = trk2EquivData == null ? null : trk2EquivData.pin
def cvv2 = trk2EquivData.cvv2

println("rest cardXferAdd rq transformer start")
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
def amountStr = amount?.toString() ?: "0"
if (amountStr.length() < 12) {
    amountStr = StringUtils.leftPadZero(amountStr, 12)
    println("amount : " + amountStr)
}

def ip = header['X-Forwarded-For'] ?: ''
//println("pin before encrypt : " + pin)
//pin = CardSystemSecurityUtil.encryptPin(pin, card)
//println("pin after encrypt : " + pin)

//JSONObject request = new JSONObject();
//request.put("cvv2", body["cvv"]);
//request.put("expiryDate", body["expiryDate"]);
//request.put("pin", body["pin"]);
//JSONObject decryptedData = CardSystemSecurityUtil.decrypt(request);

def req = [:]

req.put("serviceName", CardServiceName.CARD_XFER_ADD)

def data = [:]
data.put("cardNumber", card)
data.put("ip", StringUtils.leftPadEmpty(ip, 15))
data.put("mobileNumber", "09301677601")
data.put("stan", stan)
data.put("posData", CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA)
data.put("reference", "691199" + stan)
data.put("cvv", cvv2)
data.put("expiryDate", expiryDate)
data.put("cardAccTermId", CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
data.put("cardAccId", CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE)
data.put("dateAndTime", dateAndTime)
data.put("cardAccNameAddress", CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)
data.put("destCard", destCardNo)
data.put("amount", amountStr)
data.put("pin", pin)
data.put("accountNumber", srcAccount)
data.put("captureCode", "")

def cache = exchange.context.registry.lookupByName("transformerCacheManager");
def customerName = cache.getFromCache(CacheConstants.CACHE_NAME_DEST_CARD_CUS, card + ":" + CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
data.put("transBind", customerName == null ? " " : customerName)

req.put("data", data)

println("rest cardXferAdd rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)