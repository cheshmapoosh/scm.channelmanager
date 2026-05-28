import groovy.json.JsonOutput

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ir.daneshrefah.scm.common.transformerUtil.CardSystemSecurityUtil

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
    return !destAccount.isEmpty() && destCardNo.isEmpty();
}
def isAccountTarget = isFundTransferAccountTarget()
def isInterBankRequest = {
    if (!destCardNo.isEmpty()) {
        if (destCardNo.length() >= 6 && !destCardNo[0..5] == "DEFAULT_ACQUIRER_INSTITUTION_ID") {
            return true;
        }
    }
    return false;
}
def processCode = isAccountTarget ? "550000" : (isInterBankRequest() ? "460000" : "400000")
def transmissionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))
def localTransactionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def rrn = sprintf("%012d", System.currentTimeMillis() % 1_000_000_000_000L);
def expiryDate = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth
def pin = trk2EquivData == null ? null : trk2EquivData.pin
print("pin before encrypt : " + pin)
pin = CardSystemSecurityUtil.encryptPin(pin, srcCardNumber)
print("pin after encrypt : " + pin)

def preparePointOfServiceData = {
    def channelCode = exchange.getProperty('scmChannelCode')
    if ("MB".equalsIgnoreCase(channelCode))
        return "DEFAULT_MB_POINT_OF_SERVICE_DATA";
    else if ("NBK".equalsIgnoreCase(channelCode))
        return "DEFAULT_MB_POINT_OF_SERVICE_DATA";
    else if ("IB".equalsIgnoreCase(channelCode) || "NIB".equalsIgnoreCase(channelCode))
        return "DEFAULT_IB_POINT_OF_SERVICE_DATA";
    else if ("IVR".equalsIgnoreCase(channelCode))
        return "DEFAULT_IVR_POINT_OF_SERVICE_DATA";
    else if ("USD".equalsIgnoreCase(channelCode))
        return "DEFAULT_USSD_POINT_OF_SERVICE_DATA";
    return "DEFAULT_POINT_OF_SERVICE_DATA";
}
def serviceDataCode = preparePointOfServiceData()

def createFunctionCodeCode = {
    if (isAccountTarget)
        return fundTransfer.requestId.imEmpty() ?
                "702" : "202"
    else
        return "200";
}

def getForwardingInstitutionId = {
    if (isAccountTarget) {
        return "589463"
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

def padZeroLeft = { str, length ->
    {
        if (str.isEmpty()) {
            return "";
        }
        while (str.length() < length) {
            str = " " + str;
        }
        return str;
    }
}

def extractAdditionalData = {
    def CVV2_TAG = "P92"
    def BILL_TYPE_TAG = "BTI"
    def BILL_ID_TAG = "BBI"
    def CHECK_DUPLICATE_TAG = "DUP"
    def DST_CARD_TAG = "DST"
    def cvv2 = trk2EquivData.cvv2
    def tailoredCVV2 = trk2EquivData != null && !cvv2.isEmpty() ? fixSize(cvv2, 4) : null;

    def additionalPrivateData = ""
    if (!destAccount.isEmpty() && destCardNo.isEmpty()) {
        def functionCode = createFunctionCodeCode();
        additionalPrivateData += (!tailoredCVV2.isEmpty() ? CVV2_TAG + padZeroLeft(tailoredCVV2.length() + "", 3) + tailoredCVV2 : "");

        //202
        if (functionCode.equals("202")) {
            additionalPrivateData += BILL_TYPE_TAG + "002DI";
            additionalPrivateData += BILL_ID_TAG + padZeroLeft(fundTransfer.requestId.length() + "", 3) + fundTransfer.requestId;
        }

        //702
        if (functionCode.equals("702")) {
            additionalPrivateData += BILL_TYPE_TAG + "002NB";
        }

        additionalPrivateData += (CHECK_DUPLICATE_TAG + "001" + (body.destinationAccountCheckDuplicate ? "Y" : "N"));
    } else {
        additionalPrivateData += (!tailoredCVV2.isEmpty() ? CVV2_TAG + padZeroLeft(tailoredCVV2.length() + "", 3) + tailoredCVV2 : "");
        additionalPrivateData += (!destCardNo.isEmpty() ? DST_CARD_TAG + padZeroLeft(destCardNo.length() + "", 3) + destCardNo : "");
    }

    return additionalPrivateData.toString();
}
def additionalData = extractAdditionalData()

def req = [:]

req.put("0", "1200")
req.put("2", srcCardNumber)
req.put("3", processCode)
req.put("4", amount)
req.put("6", amount)
req.put("7", transmissionDateTime)
req.put("11", stan)
req.put("12", localTransactionDateTime);
req.put("14", expiryDate)
req.put("22", serviceDataCode)
req.put("24", createFunctionCodeCode())
req.put("26", "6012")
req.put("32", "589463")
req.put("33", getForwardingInstitutionId)
req.put("37", rrn)
req.put("41", "67777777")
req.put("42", "   777777777600")
req.put("43", "Refah Bank            Tehran       THRIR010010157171371502184852851")
if (!extractAdditionalData().isEmpty()) {
    req.put("48", additionalData)
}
req.put("49", "364")
req.put("52", pin)
if (isAccountTarget) {
    req.put("103", destAccount)
}

return JsonOutput.toJson(req)