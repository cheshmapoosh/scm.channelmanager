package transformers

import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.transformerUtil.constant.CardServiceName
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.ProcessCode
import ir.daneshrefah.scm.uaa.common.model.user.User
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils
import ir.daneshrefah.scm.utils.string.StringUtils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def body = exchange.in.body
def card = body.cardNumber
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
println("rest cardPasswordInquiry rq transformer start")

req.put("serviceName", CardServiceName.CARD_PASSWORD_INQUIRY)

def data = [:]
data.put("cardNumber", card)
data.put("stan", stan)
data.put("posData", CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA)
data.put("reference", "691199" + stan)
data.put("cardAccTermId", CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
data.put("cardAccId", CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE)
data.put("dateAndTime", dateAndTime)
data.put("expiryDate", body["expiryDate"])
data.put("cardAccNameAddress", CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)
data.put("cvv", body["cvv"])
data.put("telNbr", telNbr)
data.put("Mobnbr", mobileNumber)
data.put("ip", ip)
data.put("procCode", ProcessCode.OTP_REQUEST.getCode())
data.put("amount", StringUtils.leftPadZero("0", 12))
data.put("cad", body['cad'])

req.put("data", data)

println("rest cardPasswordInquiry rq transformer end! json body : " + JsonOutput.toJson(req))
return JsonOutput.toJson(req)