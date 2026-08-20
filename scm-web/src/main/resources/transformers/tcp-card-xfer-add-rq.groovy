import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.constant.CacheConstants
import ir.daneshrefah.scm.common.constant.TerminalType
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.FunctionCode
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ProcessCode
import org.slf4j.LoggerFactory

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def log = LoggerFactory.getLogger("CardXferAddRqGroovyTransformer")
def channelCode = exchange.getProperty("scmChannelCode")

def safeLog = { msg ->
    try {
        log.info(String.valueOf(msg))
    } catch (Exception ignored) {
        println(String.valueOf(msg))
    }
}

def isBlank = { value ->
    value == null || String.valueOf(value).trim().isEmpty()
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

def fixSize = { value, len ->
    if (value == null) {
        return ""
    }

    String text = String.valueOf(value)
    return text.length() > len ? text.substring(0, len) : text
}

def body = exchange.in.body
safeLog("card xfer add rq body : " + body)

def fundTransfer = body.fundTransfer
if (fundTransfer == null) {
    throw new RuntimeException("fundTransfer is Empty")
}

def trk2EquivData = body.trk2EquivData

def srcCardNumber = fundTransfer.sourceCardNumber
def destCardNo = fundTransfer.destinationCardNumber
def amount = fundTransfer.amount
def sourceAccountNumber = fundTransfer.sourceAccountNumber
def requestId = fundTransfer.requestId

def expiryDate = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth
def pin = trk2EquivData == null ? null : trk2EquivData.pin
def cvv2 = trk2EquivData == null ? null : trk2EquivData.cvv2

if (isBlank(srcCardNumber)) {
    throw new IllegalArgumentException("sourceCardNumber is required")
}

if (isBlank(amount)) {
    throw new IllegalArgumentException("amount is required")
}

def isFundTransferAccountTarget = {
    return !isBlank(sourceAccountNumber) && isBlank(destCardNo)
}

def isAccountTarget = isFundTransferAccountTarget()

def isInterBankRequest = {
    if (!isBlank(destCardNo)) {
        return destCardNo.length() >= 6 &&
                !destCardNo.substring(0, 6).equals(CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID)
    }

    return false
}

def processCode = isAccountTarget
        ? ProcessCode.FUND_TRANSFER_TO_ACCOUNT.getCode()
        : (isInterBankRequest()
        ? ProcessCode.FUND_TRANSFER_FROM_ACCOUNT.getCode()
        : ProcessCode.FUND_TRANSFER.getCode())

def now = LocalDateTime.now()
def transmissionDateTime = now.format(DateTimeFormatter.ofPattern("MMddHHmmss"))
def localTransactionDateTime = now.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def rrn = sprintf("%012d", System.currentTimeMillis() % 1_000_000_000_000L)

def preparePointOfServiceData = {
    safeLog("channelCode : " + channelCode)

    if (TerminalType.MB.getTerminalCode().equalsIgnoreCase(String.valueOf(channelCode))) {
        return CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA
    }

    if (TerminalType.NBK.getTerminalCode().equalsIgnoreCase(String.valueOf(channelCode))) {
        return CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA
    }

    if (TerminalType.IB.getTerminalCode().equalsIgnoreCase(String.valueOf(channelCode))
            || TerminalType.NIB.getTerminalCode().equalsIgnoreCase(String.valueOf(channelCode))) {
        return CardConstant.DEFAULT_IB_POINT_OF_SERVICE_DATA
    }

    if (TerminalType.IVR.getTerminalCode().equalsIgnoreCase(String.valueOf(channelCode))) {
        return CardConstant.DEFAULT_IVR_POINT_OF_SERVICE_DATA
    }

    if (TerminalType.USD.getTerminalCode().equalsIgnoreCase(String.valueOf(channelCode))) {
        return CardConstant.DEFAULT_USSD_POINT_OF_SERVICE_DATA
    }

    return CardConstant.DEFAULT_POINT_OF_SERVICE_DATA
}

def serviceDataCode = preparePointOfServiceData()

def createFunctionCodeCode = {
    if (isAccountTarget) {
        return isBlank(requestId)
                ? FunctionCode.PAYMENT_WITHOUT_ID_REFAH_CARD.getCode()
                : FunctionCode.PAYMENT_WITH_ID_REFAH_CARD.getCode()
    }

    return "200"
}

def getForwardingInstitutionId = {
    if (isAccountTarget) {
        return CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID
    }

    if (isBlank(destCardNo) || String.valueOf(destCardNo).length() < 6) {
        throw new IllegalArgumentException("destinationCardNumber must have at least 6 digits")
    }

    return String.valueOf(destCardNo).substring(0, 6)
}

def extractAdditionalData = {
    def CVV2_TAG = "P92"
    def BILL_TYPE_TAG = "BTI"
    def BILL_ID_TAG = "BBI"
    def CHECK_DUPLICATE_TAG = "DUP"
    def DST_CARD_TAG = "DST"

    def tailoredCVV2 = !isBlank(cvv2) ? fixSize(cvv2, 4) : ""
    def additionalPrivateData = ""

    if (isAccountTarget) {
        def functionCode = createFunctionCodeCode()

        if (!isBlank(tailoredCVV2)) {
            additionalPrivateData += tagValue(CVV2_TAG, tailoredCVV2)
        }

        if (functionCode.equals(FunctionCode.PAYMENT_WITH_ID_REFAH_CARD.getCode())) {
            additionalPrivateData += BILL_TYPE_TAG + "002DI"

            if (isBlank(requestId)) {
                throw new IllegalArgumentException("requestId is required for PAYMENT_WITH_ID_REFAH_CARD")
            }

            additionalPrivateData += tagValue(BILL_ID_TAG, requestId)
        }

        if (functionCode.equals(FunctionCode.PAYMENT_WITHOUT_ID_REFAH_CARD.getCode())) {
            additionalPrivateData += BILL_TYPE_TAG + "002NB"
        }

        additionalPrivateData += CHECK_DUPLICATE_TAG + "001" + (body.destinationAccountCheckDuplicate ? "Y" : "N")

    } else {
        if (!isBlank(tailoredCVV2)) {
            additionalPrivateData += tagValue(CVV2_TAG, tailoredCVV2)
        }

        if (!isBlank(destCardNo)) {
            additionalPrivateData += tagValue(DST_CARD_TAG, destCardNo)
        }
    }

    safeLog("field 48 additionalPrivateData length : " + additionalPrivateData.length())
    safeLog("field 48 additionalPrivateData : " + additionalPrivateData)

    return additionalPrivateData
}

def additionalData = extractAdditionalData()

def customerName = null
try {
    def cache = exchange.context.registry.lookupByName("transformerCacheManager")
    if (cache != null && !isBlank(srcCardNumber)) {
        customerName = cache.getFromCache(
                CacheConstants.CACHE_NAME_DEST_CARD_CUS,
                srcCardNumber + ":" + CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID
        )
    }
} catch (Exception e) {
    safeLog("card xfer cache error : " + e.getMessage())
}

def req = [:]
def field = [:]
def security = [:]

req.put("mti", MTI.TRANSFER_REQUEST_COMMAND.getCode())

field.put(ISOField.PAN.getPosition(), srcCardNumber)
field.put(ISOField.PROCESSING_CODE.getPosition(), processCode)
field.put(ISOField.TRANSACTION_AMOUNT.getPosition(), String.valueOf(amount))
field.put(ISOField.TRANSACTION_FEE_AMOUNT.getPosition(), String.valueOf(amount))
field.put(ISOField.TRANSMISSON_DATE_TIME.getPosition(), transmissionDateTime)
field.put(ISOField.SYSTEM_TRACE_AUDIT_NUMBER.getPosition(), stan)
field.put(ISOField.LOCAL_TRANSACTION_DATE_TIME.getPosition(), localTransactionDateTime)
field.put(ISOField.EXPIRY_DATE.getPosition(), expiryDate)
field.put(ISOField.POINT_OF_SERVICE_DATA_CODE.getPosition(), serviceDataCode)
field.put(ISOField.FUNCTION_CODE.getPosition(), createFunctionCodeCode())
field.put(ISOField.CARD_ACCEPTOR_BUSINESS_CODE.getPosition(), CardConstant.CARD_ACCEPTOR_BUSINESS_CODE)
field.put(ISOField.ACQUIRER_INSTITUTION_ID.getPosition(), CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID)
field.put(ISOField.FORWARDING_INSTITUTION_ID.getPosition(), getForwardingInstitutionId())
field.put(ISOField.RETRIEVAL_REFERENCE_NO.getPosition(), rrn)
field.put(ISOField.CARD_ACCEPT_TERMINAL_ID.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
log.info("channel code : " +channelCode)
def code = CardConstant.getCardAcceptorIdCode(channelCode)
log.info("Card Acceptor ID Code: " + code)
field.put(ISOField.CARD_ACCEPT_ID_CODE.getPosition(), code)
field.put(ISOField.CARD_ACCEPT_NAME_LOCATION.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)

if (!isBlank(additionalData)) {
    field.put(ISOField.ADDITIONAL_PRIVATE_DATA.getPosition(), additionalData)
}

field.put(ISOField.TRANSACTION_CURRENCY_CODE.getPosition(), CardConstant.DEFAULT_CURRENCY_CODE)

if (!isBlank(pin)) {
    field.put(ISOField.PIN_DATA.getPosition(), pin)
}

if (isAccountTarget) {
    field.put(ISOField.ACCOUNT_NO_2.getPosition(), sourceAccountNumber)
}

if (customerName != null) {
    field.put(ISOField.ADDITIONAL_RESPONSE_DATA2.getPosition(), String.valueOf(customerName))
}

req.put("fields", field)

security.put("expiryDate", expiryDate)
security.put("cvv2", cvv2)
security.put("pin", pin)
security.put("expiryRequired", true)
security.put("cvv2Required", true)
security.put("pinRequired", true)
security.put("macRequired", true)

req.put("security", security)

safeLog("card xfer add final request : " + req)

return JsonOutput.toJson(req)