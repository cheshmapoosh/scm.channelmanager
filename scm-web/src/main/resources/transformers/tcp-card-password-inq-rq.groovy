import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ir.daneshrefah.scm.common.constant.TerminalType
import ir.daneshrefah.scm.common.transformerUtil.PersianStringUtil
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ProcessCode
import org.slf4j.LoggerFactory

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def log = LoggerFactory.getLogger("CardPasswordNotificationGroovyTransformer")

def safeLog = { msg ->
    try {
        log.info(String.valueOf(msg))
    } catch (Exception ignored) {
        println(String.valueOf(msg))
    }
}

def requestTypeProcessCodeHashMap = [:]
requestTypeProcessCodeHashMap.put("FUND_TRANSFER", "40")
requestTypeProcessCodeHashMap.put("FUND_TRANSFER_TO_ACCOUNT", "55")
requestTypeProcessCodeHashMap.put("PAYMENT", "50")
requestTypeProcessCodeHashMap.put("GET_BALANCE", "31")
requestTypeProcessCodeHashMap.put("BILL_PAYMENT", "50")
requestTypeProcessCodeHashMap.put("MINI_STATEMENT", "38")
requestTypeProcessCodeHashMap.put("AUTHENTICATION", "30")
requestTypeProcessCodeHashMap.put("AUTHENTICATION_TOPUP_PAYMENT", "30")
requestTypeProcessCodeHashMap.put("AUTHENTICATION_LOAN_PAYMENT", "30")
requestTypeProcessCodeHashMap.put("AUTHENTICATION_PACKAGE_PAYMENT", "30")
requestTypeProcessCodeHashMap.put("CARD_LESS_ADD", "12")
requestTypeProcessCodeHashMap.put("CARD_LESS_CANCEL", "16")
requestTypeProcessCodeHashMap.put("MODIFY_CARD_FIRST_PASS", "91")
requestTypeProcessCodeHashMap.put("DYNAMIC_PIN", "92")

def isBlank = { value ->
    value == null || String.valueOf(value).trim().isEmpty()
}

def normalizeRequestType = { value ->
    if (value == null) {
        return null
    }
    return String.valueOf(value)
}

def normalizeAdditionalInformation = { value ->
    if (value == null) {
        return [:]
    }

    if (value instanceof Map) {
        return value
    }

    if (value instanceof String) {
        String text = value.trim()
        if (text.isEmpty()) {
            return [:]
        }

        def parsed = new JsonSlurper().parseText(text)
        if (parsed instanceof Map) {
            return parsed
        }

        throw new IllegalArgumentException("additionalInformation must be a JSON object")
    }

    throw new IllegalArgumentException("Unsupported additionalInformation type: " + value.getClass().getName())
}

def fixSize = { value, len ->
    if (value == null) {
        return ""
    }

    String text = String.valueOf(value)
    if (text.isEmpty()) {
        return ""
    }

    return text.length() > len ? text.substring(0, len) : text
}

def padZero3 = { value ->
    String.valueOf(value).padLeft(3, '0')
}

def tagValue = { tag, value ->
    if (value == null) {
        return ""
    }

    String text = String.valueOf(value)
    return tag + padZero3(text.length()) + text
}

def computeBillType = { value ->
    String billId = value == null ? "" : String.valueOf(value)

    if (billId.isEmpty()) {
        return "قبض عمومی"
    }

    if ("11".equals(billId)) {
        return "انتقال وجه کارتی"
    }

    if (billId.length() >= 17) {
        return "قبض تامین اجتماعی"
    }

    if (billId.length() < 2) {
        return ""
    }

    def billTypeNumber = billId.substring(billId.length() - 2, billId.length() - 1)
    def originalBillType = String.valueOf(billTypeNumber).padLeft(2, '0')

    if ("01".equals(originalBillType)) {
        return "قبض آب"
    }

    if ("02".equals(originalBillType)) {
        return "قبض برق"
    }

    if ("03".equals(originalBillType)) {
        return "قبض گاز"
    }

    if ("04".equals(originalBillType)) {
        return "قبض تلفن ثابت"
    }

    if ("05".equals(originalBillType)) {
        return "قبض تلفن همراه"
    }

    if ("06".equals(originalBillType)) {
        return "عوارض شهرداری"
    }

    if ("09".equals(originalBillType)) {
        return "قبض جریمه راهنمایی و رانندگی"
    }

    if ("11".equals(originalBillType)) {
        return "انتقال وجه کارتی"
    }

    return ""
}

def body = exchange.in.body
safeLog("card password notification rq body : " + body)

def card = body.card
if (card == null) {
    throw new IllegalArgumentException("card is required")
}

def trk2EquivData = body.trk2EquivData
def additionalInformation = normalizeAdditionalInformation(body.additionalInformation)

def pin = trk2EquivData == null ? null : trk2EquivData.pin
def cvv2 = trk2EquivData == null ? null : trk2EquivData.cvv2
def cardExpirationYearMonth = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth

def srcCard = card.sourceCardNumber
def reqType = normalizeRequestType(body.requestType)
def amount = body.amount



if (isBlank(srcCard)) {
    throw new IllegalArgumentException("sourceCardNumber is required")
}

if (String.valueOf(srcCard).length() < 6) {
    throw new IllegalArgumentException("sourceCardNumber must have at least 6 digits")
}

if (isBlank(reqType)) {
    throw new IllegalArgumentException("requestType is required")
}

def processCodeFor48 = requestTypeProcessCodeHashMap.get(reqType)
if (isBlank(processCodeFor48)) {
    throw new IllegalArgumentException("Unsupported requestType for field 48: " + reqType)
}

def now = LocalDateTime.now()
def transmissionDateTime = now.format(DateTimeFormatter.ofPattern("MMddHHmmss"))
def captureDate = now.format(DateTimeFormatter.ofPattern("MMdd"))
def localTransactionDateTime = now.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))

def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def rrn = sprintf("%012d", System.currentTimeMillis() % 1_000_000_000_000L)

def fillAdditionalInformation = {
    def additionalPrivateData = ""

    def tailoredCVV2 = !isBlank(cvv2) ? fixSize(cvv2, 4) : ""

    if (!tailoredCVV2.isEmpty()) {
        additionalPrivateData += tagValue("P92", tailoredCVV2)
    }

    additionalPrivateData += tagValue("PRC", processCodeFor48)

    if (additionalInformation != null
            && !additionalInformation.isEmpty()
            && (additionalInformation.containsKey("billId") || additionalInformation.containsKey("destinationCardNumber"))) {

        if ("FUND_TRANSFER".equals(reqType)) {
            def destinationCardNumber = additionalInformation.get("destinationCardNumber")
            safeLog("destinationCardNumber : " + destinationCardNumber)

            if (!isBlank(destinationCardNumber)) {
                String destination = String.valueOf(destinationCardNumber)
                additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(destination))
            }

        } else if ("PAYMENT".equals(reqType) || "BILL_PAYMENT".equals(reqType)) {
            def billId = additionalInformation.get("billId")
            safeLog("billId : " + billId)

            if (isBlank(billId)) {
                throw new RuntimeException("Error in processing billID with data: <<" + billId + ">>")
            }

            def billType = computeBillType(billId)
            additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(billType))

        } else if ("GET_BALANCE".equals(reqType)) {
            def message = "مانده گیری"
            additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(message))

        } else if ("MINI_STATEMENT".equals(reqType)) {
            def message = "گردش حساب"
            additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(message))
        }

    } else {
        def billType
        def message

        switch (reqType) {
            case "PAYMENT":
            case "BILL_PAYMENT":
                billType = computeBillType("")
                additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(billType))
                break

            case "MINI_STATEMENT":
                message = "گردش حساب"
                additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(message))
                break

            case "FUND_TRANSFER":
                billType = computeBillType("11")
                additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(billType))
                break

            case "GET_BALANCE":
                message = "مانده گیری"
                additionalPrivateData += tagValue("CAD", PersianStringUtil.cvrtUTFToAscii1256Encoding(message))
                break
        }
    }

    safeLog("field 48 length : " + additionalPrivateData.length())
    safeLog("field 48 additionalPrivateData : " + additionalPrivateData)

    return additionalPrivateData
}

def req = [:]
def field = [:]
def security = [:]

req.put("mti", MTI.AUTHORIZATION_ADVICE_REQUEST_COMMAND.getCode())

field.put(ISOField.PAN.getPosition(), srcCard)
field.put(ISOField.PROCESSING_CODE.getPosition(), ProcessCode.CARD_PASSWORD_NOTIFICATION.getCode())
field.put(ISOField.TRANSMISSON_DATE_TIME.getPosition(), transmissionDateTime)
field.put(ISOField.SYSTEM_TRACE_AUDIT_NUMBER.getPosition(), stan)
field.put(ISOField.LOCAL_TRANSACTION_DATE_TIME.getPosition(), localTransactionDateTime)
field.put(ISOField.CARD_ACCEPTOR_BUSINESS_CODE.getPosition(), CardConstant.CARD_ACCEPTOR_BUSINESS_CODE)
field.put(ISOField.CAPTURE_DATE.getPosition(), captureDate)
field.put(ISOField.ACQUIRER_INSTITUTION_ID.getPosition(), CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID)
field.put(ISOField.ACQUIRE_COUNTRY_CODE.getPosition(), CardConstant.DEFAULT_CURRENCY_CODE)
field.put(ISOField.POINT_OF_SERVICE_DATA_CODE.getPosition(), CardConstant.DEFAULT_IB_POINT_OF_SERVICE_DATA)

if ("DYNAMIC_PIN".equals(reqType)) {
    field.put(ISOField.FUNCTION_CODE.getPosition(), CardConstant.DYNAMIC_PIN_FUNCTION_CODE)
} else {
    field.put(ISOField.FUNCTION_CODE.getPosition(), CardConstant.FUNCTION_CODE)
}

field.put(ISOField.FORWARDING_INSTITUTION_ID.getPosition(), String.valueOf(srcCard).substring(0, 6))
field.put(ISOField.RETRIEVAL_REFERENCE_NO.getPosition(), rrn)

def channelCode = exchange.getProperty("scmChannelCode")
def isNBKChannel = TerminalType.NBK.getTerminalCode().equalsIgnoreCase(String.valueOf(channelCode))

if (isNBKChannel && "BILL_PAYMENT".equals(reqType)) {
    field.put(ISOField.CARD_ACCEPT_TERMINAL_ID.getPosition(), CardConstant.BPG_CARD_ACCEPT_TERMINAL_ID)
} else {
    field.put(ISOField.CARD_ACCEPT_TERMINAL_ID.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
}
log.info("channel code : " +channelCode)
def code = CardConstant.getCardAcceptorIdCode(channelCode)
log.info("Card Acceptor ID Code: " + code)
field.put(ISOField.CARD_ACCEPT_ID_CODE.getPosition(), code)
field.put(ISOField.CARD_ACCEPT_NAME_LOCATION.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)
field.put(ISOField.ACQUIRE_INSTITUTE_CODE.getPosition(), CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID)
field.put(ISOField.TRANSACTION_CURRENCY_CODE.getPosition(), CardConstant.DEFAULT_CURRENCY_CODE)

def additionalPrivateData = ""

if (!isBlank(pin)) {
    field.put(ISOField.PIN_DATA.getPosition(), pin)
} else {
    additionalPrivateData = fillAdditionalInformation()
}

if (amount != null) {
    field.put(ISOField.TRANSACTION_AMOUNT.getPosition(), String.valueOf(amount))
    field.put(ISOField.TRANSACTION_FEE_AMOUNT.getPosition(), String.valueOf(amount))
}

if (!isBlank(additionalPrivateData)) {
    field.put(ISOField.ADDITIONAL_PRIVATE_DATA.getPosition(), additionalPrivateData)
}

if (TerminalType.IVR.getTerminalCode().equals(String.valueOf(channelCode))) {
    field.put(ISOField.EXPIRY_DATE.getPosition(), "0000")
} else {
    field.put(ISOField.EXPIRY_DATE.getPosition(), !isBlank(cardExpirationYearMonth) ? cardExpirationYearMonth : null)
}

req.put("fields", field)

security.put("expiryDate", cardExpirationYearMonth)
security.put("cvv2", cvv2)
security.put("pin", pin)
security.put("expiryRequired", true)
security.put("cvv2Required", true)
security.put("pinRequired", !isBlank(pin))
security.put("macRequired", true)

req.put("security", security)

safeLog("card password notification final request : " + req)

return JsonOutput.toJson(req)