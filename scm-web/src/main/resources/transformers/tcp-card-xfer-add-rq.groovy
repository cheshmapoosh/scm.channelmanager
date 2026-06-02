import groovy.json.JsonOutput
import ir.daneshrefah.scm.common.constant.TerminalType
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.FunctionCode
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ProcessCode;
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ir.daneshrefah.scm.common.transformerUtil.CardSystemSecurityUtil
import ir.daneshrefah.scm.utils.string.StringUtils;

def body = exchange.in.body
def fundTransfer = body.fundTransfer
if (fundTransfer == null) {
    throw new RuntimeException("fundTransfer is Empty")
}
def trk2EquivData = body.trk2EquivData
def srcCardNumber = fundTransfer.sourceCardNumber
def destCardNo = fundTransfer.destinationCardNumber
def amount = fundTransfer.amount
def destAccount = fundTransfer.destinationAccountNo

def isFundTransferAccountTarget = {
    println("destAccount : " + destAccount + "destCardNo : " + destCardNo)
    println("isFundTransferAccountTarget : " + destAccount && destCardNo)
    return destAccount && destCardNo//destAccount != null && !destAccount.isEmpty() && destCardNo != null && destCardNo.isEmpty();
}
def isAccountTarget = isFundTransferAccountTarget()
println("isAccountTarget : " + isAccountTarget)
def isInterBankRequest = {
    println("isInterBankRequest destCardNo " + destCardNo)
    if (!destCardNo.isEmpty()) {
        if (destCardNo.length() >= 6 && !destCardNo[0..5] == CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID) {
            println("isInterBankRequest true")
            return true;
        }
    }
    println("isInterBankRequest false")
    return false;
}
def processCode = isAccountTarget ? ProcessCode.FUND_TRANSFER_TO_ACCOUNT.getCode() : (isInterBankRequest() ? ProcessCode.FUND_TRANSFER_FROM_ACCOUNT.getCode() : ProcessCode.FUND_TRANSFER.getCode())
println("processCode : " + processCode)
def transmissionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))
def localTransactionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def rrn = sprintf("%012d", System.currentTimeMillis() % 1_000_000_000_000L);
def expiryDate = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth
def pin = trk2EquivData == null ? null : trk2EquivData.pin
println("pin before encrypt : " + pin)
pin = CardSystemSecurityUtil.encryptPin(pin, srcCardNumber)
println("pin after encrypt : " + pin)

println("before preparePointOfServiceData : ")
def preparePointOfServiceData = {
    def channelCode = exchange.getProperty('scmChannelCode')
    println("channelCode : " + channelCode)
    if (TerminalType.MB.getTerminalCode().equalsIgnoreCase(channelCode))
        return CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA;
    else if (TerminalType.NBK.getTerminalCode().equalsIgnoreCase(channelCode))
        return CardConstant.DEFAULT_MB_POINT_OF_SERVICE_DATA;
    else if (TerminalType.IB.getTerminalCode().equalsIgnoreCase(channelCode) || TerminalType.NIB.getTerminalCode().equalsIgnoreCase(channelCode))
        return CardConstant.DEFAULT_IB_POINT_OF_SERVICE_DATA
    else if (TerminalType.IVR.getTerminalCode().equalsIgnoreCase(channelCode))
        return CardConstant.DEFAULT_IVR_POINT_OF_SERVICE_DATA
    else if (TerminalType.USD.getTerminalCode().equalsIgnoreCase(channelCode))
        return CardConstant.DEFAULT_USSD_POINT_OF_SERVICE_DATA
    return CardConstant.DEFAULT_POINT_OF_SERVICE_DATA
}
def serviceDataCode = preparePointOfServiceData()
println("serviceDataCode : " + serviceDataCode)

def createFunctionCodeCode = {
    if (isAccountTarget) {
        println("fundTransfer : " + fundTransfer + "fundTransfer.requestId  : " + fundTransfer.requestId)
        return fundTransfer != null && fundTransfer.requestId != null && fundTransfer.requestId.isEmpty() ?
                FunctionCode.PAYMENT_WITHOUT_ID_REFAH_CARD.getCode() : FunctionCode.PAYMENT_WITH_ID_REFAH_CARD.getCode();
    } else
        return "200";
}

def getForwardingInstitutionId = {
    if (isAccountTarget) {
        return CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID
    }
    destCardNo[0..5]
}

def fixSize = { str, len ->
    {
        if (!str.isEmpty()) {
            str.length() > len ? str[0..len - 1] : str
        } else {
            str
        }
    }
}

def extractAdditionalData = {
    def CVV2_TAG = "P92"
    def BILL_TYPE_TAG = "BTI"
    def BILL_ID_TAG = "BBI"
    def CHECK_DUPLICATE_TAG = "DUP"
    def DST_CARD_TAG = "DST"
    def cvv2 = trk2EquivData.cvv2
    println("cvv2 :" + cvv2)
    def tailoredCVV2 = trk2EquivData != null && cvv2 != null && !cvv2.isEmpty() ? fixSize(cvv2, 4) : null;

    def additionalPrivateData = ""
    println("additionalPrivateData destAccount: " + destAccount + "destCardNo : " + destCardNo)
    if (destAccount != null && !destAccount.isEmpty() && (destCardNo == null || destCardNo.isEmpty())) {
        def functionCode = createFunctionCodeCode();
        println("additionalPrivateData 1 : " + additionalPrivateData + "tailoredCVV2: " + tailoredCVV2)
        additionalPrivateData += (tailoredCVV2 != null && !tailoredCVV2.isEmpty() ? CVV2_TAG + StringUtils.leftPadZero(tailoredCVV2.length() + "", 3) + tailoredCVV2 : "");

        println("additionalPrivateData 2 : " + additionalPrivateData)

        //202
        if (FunctionCode.PAYMENT_WITH_ID_REFAH_CARD.getCode()) {
            additionalPrivateData += BILL_TYPE_TAG + "002DI";
            additionalPrivateData += BILL_ID_TAG + StringUtils.leftPadEmpty(String.valueOf(fundTransfer.requestId.length()) + "", 3) + fundTransfer.requestId;
        }
        println("additionalPrivateData 3 : " + additionalPrivateData)

        //702
        if (functionCode.equals(FunctionCode.PAYMENT_WITHOUT_ID_REFAH_CARD.getCode())) {
            additionalPrivateData += BILL_TYPE_TAG + "002NB";
        }
        println("additionalPrivateData 4 : " + additionalPrivateData)

        additionalPrivateData += (CHECK_DUPLICATE_TAG + "001" + (body.destinationAccountCheckDuplicate ? "Y" : "N"));
        println("additionalPrivateData 5 : " + additionalPrivateData)
    } else {
        println("additionalPrivateData 6 : " + additionalPrivateData + "tailoredCVV2:" + tailoredCVV2)
        additionalPrivateData += (tailoredCVV2 != null && !tailoredCVV2.isEmpty() ? CVV2_TAG + StringUtils.leftPadEmpty(String.valueOf(tailoredCVV2.length()) + "", 3) + tailoredCVV2 : "");
        println("additionalPrivateData 7 : " + additionalPrivateData)
        println("destCardNo 7 : "+destCardNo)
        additionalPrivateData += (destCardNo != null && !destCardNo.isEmpty() ? DST_CARD_TAG + StringUtils.leftPadEmpty(String.valueOf(destCardNo.length()) + "", 3) + destCardNo : "");
        println("additionalPrivateData 8 : " + additionalPrivateData)
    }

    return additionalPrivateData.toString();
}
def additionalData = extractAdditionalData()

def req = [:]

req.put("mti", MTI.TRANSFER_REQUEST_COMMAND.getCode())
req.put(ISOField.PAN.getPosition(), srcCardNumber)
req.put(ISOField.PROCESSING_CODE.getPosition(), processCode)
req.put(ISOField.TRANSACTION_AMOUNT.getPosition(), amount)
req.put(ISOField.TRANSACTION_FEE_AMOUNT.getPosition(), amount)
req.put(ISOField.TRANSMISSON_DATE_TIME.getPosition(), transmissionDateTime)
req.put(ISOField.SYSTEM_TRACE_AUDIT_NUMBER.getPosition(), stan)
req.put(ISOField.LOCAL_TRANSACTION_DATE_TIME.getPosition(), localTransactionDateTime);
req.put(ISOField.EXPIRY_DATE.getPosition(), expiryDate)
req.put(ISOField.POINT_OF_SERVICE_DATA_CODE.getPosition(), serviceDataCode)
req.put(ISOField.FUNCTION_CODE.getPosition(), createFunctionCodeCode())
req.put(ISOField.CARD_ACCEPTOR_BUSINESS_CODE.getPosition(), CardConstant.CARD_ACCEPTOR_BUSINESS_CODE)
req.put(ISOField.ACQUIRER_INSTITUTION_ID.getPosition(), CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID)
req.put(ISOField.FORWARDING_INSTITUTION_ID.getPosition(), getForwardingInstitutionId)
req.put(ISOField.RETRIEVAL_REFERENCE_NO.getPosition(), rrn)
req.put(ISOField.CARD_ACCEPT_TERMINAL_ID.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID)
req.put(ISOField.CARD_ACCEPT_ID_CODE.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE)
req.put(ISOField.CARD_ACCEPT_NAME_LOCATION.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION)
def additional = extractAdditionalData()
if (additional != null && !additional.isEmpty()) {
    req.put(ISOField.ADDITIONAL_PRIVATE_DATA.getPosition(), additionalData)
}
req.put(ISOField.TRANSACTION_CURRENCY_CODE.getPosition(), CardConstant.DEFAULT_CURRENCY_CODE)
req.put(ISOField.PIN_DATA.getPosition(), pin)
if (isAccountTarget) {
    req.put(ISOField.ACCOUNT_NO_2.getPosition(), destAccount)
}

println("card xfer add rs : " + req)
return JsonOutput.toJson(req)