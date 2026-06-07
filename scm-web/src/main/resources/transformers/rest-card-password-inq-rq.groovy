package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.transformerUtil.CardSystemSecurityUtil
import ir.daneshrefah.scm.common.transformerUtil.constant.CardServiceName
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.ProcessCode
import ir.daneshrefah.scm.uaa.common.model.user.User
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils
import ir.daneshrefah.scm.utils.string.StringUtils
import org.json.simple.JSONObject

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def body = exchange.in.body
println("rest cardPasswordInquiry rq transformer start body :" + body)
def cardBody = body.card
println("card body : "+cardBody)
def trk2EquivData = body.trk2EquivData
println("trk2EquivData body : "+trk2EquivData)
def additionalInformation = body.additionalInformation
def pin = trk2EquivData == null ? null : trk2EquivData.pin
def cvv2 = trk2EquivData == null ? null : trk2EquivData.cvv2
def cardExpirationYearMonth = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth
def srcCard = cardBody.sourceCardNumber
def reqType = body.requestType
def card = cardBody.sourceCardNumber
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
def user = (User) AuthenticationUtils.getAuthentication().getPrincipal()
def accessParameter = user.getAccessParameters()
def mobileNumber = accessParameter[0]
def telNbr = user.getPerson().getPhone1()
def ip = ""
if (mobileNumber.isEmpty() && telNbr.isEmpty() && ip.isEmpty()) {
    throw new RuntimeException("mobileNumber & telNbr & ip is empty!")
}

def channelCode = exchange.getProperty('scmChannelCode')
if ("MB".equalsIgnoreCase(channelCode) && mobileNumber.isEmpty()) {
    throw new RuntimeException("mobileNumber is empty!")
} else if ("NIB".equalsIgnoreCase(channelCode) || "IB".equalsIgnoreCase(channelCode)) {} {
    throw new RuntimeException("ip is empty!")
} else if ("IVR".equalsIgnoreCase(channelCode)) {
    throw new RuntimeException("telNumber is empty!")
}

def req = [:]
req.put("serviceName", CardServiceName.CARD_PASSWORD_INQUIRY)

def data = [:]
data.put("cardNumber", card)
data.put("stan", stan)
data.put("posData", CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA)
data.put("reference", "691199" + stan)
data.put("cardAccTermId", CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
data.put("cardAccId", CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE)
data.put("dateAndTime", dateAndTime)
data.put("expiryDate", cardExpirationYearMonth)
data.put("cardAccNameAddress", CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)
data.put("cvv", cvv2)
data.put("telNbr", telNbr)
data.put("Mobnbr", mobileNumber)
data.put("ip", ip)
data.put("procCode", ProcessCode.OTP_REQUEST.getCode()[0..1])
println("procCode : "+ ProcessCode.OTP_REQUEST.getCode() + "resti : "+ ProcessCode.OTP_REQUEST.getCode()[0..1])
data.put("amount", StringUtils.leftPadZero("0", 12))
data.put("cad", "")//???????????????????/az cache byd biad - nam crd inq

req.put("data", data)

println("rest cardPasswordInquiry rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)