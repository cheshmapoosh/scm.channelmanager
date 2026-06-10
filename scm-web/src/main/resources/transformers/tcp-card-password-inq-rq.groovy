import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ir.daneshrefah.scm.common.constant.TerminalType
import ir.daneshrefah.scm.common.model.customer.Card

import ir.daneshrefah.scm.common.transformerUtil.PersianStringUtil
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.utils.string.StringUtils;
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.RequestType
import ir.daneshrefah.scm.provider.shetab.iso.util.ProcessCode

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

def body = exchange.in.body
println("card password inq rq : " + body)
def card = body.card
def trk2EquivData = body.trk2EquivData
def additionalInformation = body.additionalInformation
def pin = trk2EquivData == null ? null : trk2EquivData.pin
def cvv2 = trk2EquivData == null ? null : trk2EquivData.cvv2
def cardExpirationYearMonth = trk2EquivData == null ? null : trk2EquivData.cardExpirationYearMonth
def srcCard = card.sourceCardNumber
def reqType = body.requestType
def transmissionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"))
def captureDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMdd"))
def localTransactionDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
def stan = sprintf("%06d", System.currentTimeMillis() % 1_000_000)
def rrn = sprintf("%012d", System.currentTimeMillis() % 1_000_000_000_000L);
def amount = body.amount

def fixSize = { str, len ->
    {
        if (!str.isEmpty()) {
            str.length() > len ? str[0..len - 1] : str
        } else {
            str
        }
    }
}

//def padZeroLeft = { str, length ->
//    {
//        if (str.isEmpty()) {
//            return "";
//        }
//        while (str.length() < length) {
//            str = " " + str;
//        }
//        return str;
//    }
//}


def computeBillType = { billId ->
    {
        if (billId.isEmpty()) {
            return "قبض عمومی"
        } else if (billId.equals("11")) {
            return "انتقال وجه کارتی"
        } else if (billId.length() >= 17) {
            return "قبض تامین اجتماعی"
        }

        def billTypeNumber = billId[billId.length() - 2..billId.length() - 2];
        def originalBillType = StringUtils.leftPad(String.valueOf(billTypeNumber), 2, ' ')

        if (originalBillType.equals("01")) {
            return "قبض آب";
        } else if (originalBillType.equals("02")) {
            return "قبض برق"
        } else if (originalBillType.equals("03")) {
            return "قبض گاز";
        } else if (originalBillType.equals("04")) {
            return "قبض تلفن ثابت"
        } else if (originalBillType.equals("05")) {
            return "قبض تلفن همراه"
        } else if (originalBillType.equals("06")) {
            return "عوارض شهرداری"
        } else if (originalBillType.equals("09")) {
            return "قبض جریمه راهنمایی و رانندگی"
        } else if (originalBillType.equals("11")) {
            return "انتقال وجه کارتی"
        } else {
            return ""
//            return originalBillType.equals("07") ? properties.getProperty("TX")
//                    : properties.getProperty("UD");
        }
    }
}


def fillAdditionalInformation = {
    def additionalPrivateData = "";
    def processCode = requestTypeProcessCodeHashMap.get(reqType)
    def tailoredCVV2 = trk2EquivData != null && cvv2 != null && !cvv2.isEmpty() ? fixSize(cvv2, 4) : "";
    println("---step 1---")
    if (!tailoredCVV2.isEmpty()) {
        println("---step 12---")
        additionalPrivateData = "P92" + cvv2 + tailoredCVV2
        println("---step 13---")
        println("process code : " + processCode)
        println("pad left : " + StringUtils.leftPadEmpty(String.valueOf(processCode), 3.intValue()))
        additionalPrivateData += "PRC" + StringUtils.leftPadEmpty(String.valueOf(processCode), 3.intValue()) + processCode
    }
    println("---step 14---")

    if (additionalInformation != null && !additionalInformation.isEmpty()
            && (additionalInformation.containsKey("billId") || additionalInformation.containsKey("destinationCardNumber"))) {
        if (reqType == RequestType.FUND_TRANSFER) {
            def destinationCardNumber = additionalInformation.get("destinationCardNumber");
            if (destinationCardNumber != null && !destinationCardNumber.isEmpty()) {
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(destinationCardNumber.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(destinationCardNumber);
            } else if (reqType == RequestType.PAYMENT || reqType == RequestType.BILL_PAYMENT) {
                def billId = additionalInformation.get("billId");
                if (billId == null || billId.isEmpty()) {
                    throw new RuntimeException("Error in processing billID with data: <<" + billId + ">>");
                }
                def billType = computeBillType(billId);
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(billType.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(billType)
            } else if (reqType == RequestType.GET_BALANCE) {
                def message = "مانده گیری"
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(message.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(message);
            } else if (reqType == RequestType.MINI_STATEMENT) {
                def message = "گردش حساب"
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(message.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(message);
            }
        }
    } else {
        def billType;
        def message;
        switch (reqType) {
            case RequestType.PAYMENT:
            case RequestType.BILL_PAYMENT:
                billType = computeBillType("");
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(billType.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(billType);
                break;
            case RequestType.MINI_STATEMENT:
                message = "گردش حساب"
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(message.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(message);
                break;
            case RequestType.FUND_TRANSFER:
                billType = computeBillType("11");
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(billType.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(billType);
                break;
            case RequestType.GET_BALANCE:
                message = "مانده گیری"
                additionalPrivateData += "CAD" + StringUtils.leftPadEmpty(String.valueOf(message.length()), 3) + PersianStringUtil.cvrtUTFToAscii1256Encoding(message);
                break;
        }
    }
    println("additionalPrivateData : " + additionalPrivateData.toString())
    return additionalPrivateData.toString();
}


def req = [:]
def field = [:]
def security = [:]

req.put("mti", MTI.AUTHORIZATION_ADVICE_REQUEST_COMMAND.getCode());

field.put("mti", MTI.AUTHORIZATION_ADVICE_REQUEST_COMMAND.getCode());
field.put(ISOField.PAN.getPosition(), srcCard);
field.put(ISOField.PROCESSING_CODE.getPosition(), ProcessCode.CARD_PASSWORD_NOTIFICATION.getCode());
field.put(ISOField.TRANSMISSON_DATE_TIME.getPosition(), transmissionDateTime);
field.put(ISOField.SYSTEM_TRACE_AUDIT_NUMBER.getPosition(), stan);
field.put(ISOField.LOCAL_TRANSACTION_DATE_TIME.getPosition(), localTransactionDateTime);
field.put(ISOField.CARD_ACCEPTOR_BUSINESS_CODE.getPosition(), CardConstant.CARD_ACCEPTOR_BUSINESS_CODE);
field.put(ISOField.CAPTURE_DATE.getPosition(), captureDate);
field.put(ISOField.ACQUIRER_INSTITUTION_ID.getPosition(), CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID);
field.put(ISOField.ACQUIRE_COUNTRY_CODE.getPosition(), CardConstant.DEFAULT_CURRENCY_CODE);
field.put(ISOField.POINT_OF_SERVICE_DATA_CODE.getPosition(), CardConstant.DEFAULT_IB_POINT_OF_SERVICE_DATA);
field.put(ISOField.FUNCTION_CODE.getPosition(), reqType == RequestType.DYNAMIC_PIN ? CardConstant.DYNAMIC_PIN_FUNCTION_CODE : CardConstant.FUNCTION_CODE);
field.put(ISOField.FORWARDING_INSTITUTION_ID.getPosition(), srcCard[0..5]);
field.put(ISOField.RETRIEVAL_REFERENCE_NO.getPosition(), rrn);

def channelCode = exchange.getProperty('scmChannelCode')
def isNBKChannel = TerminalType.NBK.getTerminalCode().equalsIgnoreCase(channelCode);
if (isNBKChannel && Objects.equals(RequestType.BILL_PAYMENT, reqType)) {
    field.put(ISOField.CARD_ACCEPT_TERMINAL_ID.getPosition(), CardConstant.BPG_CARD_ACCEPT_TERMINAL_ID);
} else {
    field.put(ISOField.CARD_ACCEPT_TERMINAL_ID.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID);
}
field.put(ISOField.CARD_ACCEPT_ID_CODE.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_ID_CODE);
field.put(ISOField.CARD_ACCEPT_NAME_LOCATION.getPosition(), CardConstant.DEFAULT_CARD_ACCEPT_NAME_LOCATION);
field.put(ISOField.ACQUIRE_INSTITUTE_CODE.getPosition(), CardConstant.DEFAULT_ACQUIRER_INSTITUTION_ID);
field.put(ISOField.TRANSACTION_CURRENCY_CODE.getPosition(), CardConstant.DEFAULT_CURRENCY_CODE);

def additionalPrivateData = "";
if (pin != null && !pin.isEmpty()) {
//    req.put(ISOField.PIN_DATA.getPosition(), CardSystemSecurityUtil.encryptPin(pin, srcCard));
    field.put(ISOField.PIN_DATA.getPosition(), pin);
} else {
    additionalPrivateData = fillAdditionalInformation();
}
if (amount != null) {
    field.put(ISOField.TRANSACTION_AMOUNT.getPosition(), amount)
    field.put(ISOField.TRANSACTION_FEE_AMOUNT.getPosition(), amount)
}

if (!additionalPrivateData.toString().isEmpty()) {
    field.put(ISOField.ADDITIONAL_PRIVATE_DATA.getPosition(), additionalPrivateData)
}

if (channelCode == TerminalType.IVR.getTerminalCode()) {
    field.put(ISOField.EXPIRY_DATE.getPosition(), "0000")
} else {
    field.put(ISOField.EXPIRY_DATE.getPosition(), trk2EquivData != null && cardExpirationYearMonth != null ? cardExpirationYearMonth : null)
}
req.put("fields", field)


security.put("expiryDate", cardExpirationYearMonth);
security.put("cvv2", cvv2);
security.put("pin", pin);
security.put("expiryRequired", true);
security.put("cvv2Required", true);
security.put("pinRequired", true);
security.put("macRequired", false);

req.put("security", security)

println("card pass inq rq :  " + req)

return JsonOutput.toJson(req)