import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
def PIN_KEY = "hps_pin_key";
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

def cvrtUTFToAscii1256Encoding = { src ->
    {
        def dest = "";

        for (def index = 0; index < src.length(); ++index) {
            switch (src.charAt(index)) {
                case '،':
                    dest = dest + '¡';
                    break;
                case '؛':
                    dest = dest + 'º';
                    break;
                case '؟':
                    dest = dest + '¿';
                    break;
                case 'ء':
                    dest = dest + 'Á';
                    break;
                case 'آ':
                    dest = dest + 'Â';
                    break;
                case 'أ':
                    dest = dest + 'Ã';
                    break;
                case 'ؤ':
                    dest = dest + 'Ä';
                    break;
                case 'إ':
                    dest = dest + 'Å';
                    break;
                case 'ئ':
                    dest = dest + 'Æ';
                    break;
                case 'ا':
                    dest = dest + 'Ç';
                    break;
                case 'ب':
                    dest = dest + 'È';
                    break;
                case 'ت':
                    dest = dest + 'Ê';
                    break;
                case 'ث':
                    dest = dest + 'Ë';
                    break;
                case 'ج':
                    dest = dest + 'Ì';
                    break;
                case 'ح':
                    dest = dest + 'Í';
                    break;
                case 'خ':
                    dest = dest + 'Î';
                    break;
                case 'د':
                    dest = dest + 'Ï';
                    break;
                case 'ذ':
                    dest = dest + 'Ð';
                    break;
                case 'ر':
                    dest = dest + 'Ñ';
                    break;
                case 'ز':
                    dest = dest + 'Ò';
                    break;
                case 'س':
                    dest = dest + 'Ó';
                    break;
                case 'ش':
                    dest = dest + 'Ô';
                    break;
                case 'ص':
                    dest = dest + 'Õ';
                    break;
                case 'ض':
                    dest = dest + 'Ö';
                    break;
                case 'ط':
                    dest = dest + 'Ø';
                    break;
                case 'ظ':
                    dest = dest + 'Ù';
                    break;
                case 'ع':
                    dest = dest + 'Ú';
                    break;
                case 'غ':
                    dest = dest + 'Û';
                    break;
                case 'ف':
                    dest = dest + 'Ý';
                    break;
                case 'ق':
                    dest = dest + 'Þ';
                    break;
                case 'ك':
                    dest = dest + 'ß';
                    break;
                case 'ل':
                    dest = dest + 'á';
                    break;
                case 'م':
                    dest = dest + 'ã';
                    break;
                case 'ن':
                    dest = dest + 'ä';
                    break;
                case 'ه':
                    dest = dest + 'å';
                    break;
                case 'و':
                    dest = dest + 'æ';
                    break;
                case 'ى':
                    dest = dest + 'ì';
                    break;
                case 'ي':
                    dest = dest + 'í';
                    break;
                case '٠':
                    dest = dest + '0';
                    break;
                case '١':
                    dest = dest + '1';
                    break;
                case '٢':
                    dest = dest + '2';
                    break;
                case '٣':
                    dest = dest + '3';
                    break;
                case '٤':
                    dest = dest + '4';
                    break;
                case '٥':
                    dest = dest + '5';
                    break;
                case '٦':
                    dest = dest + '6';
                    break;
                case '٧':
                    dest = dest + '7';
                    break;
                case '٨':
                    dest = dest + '8';
                    break;
                case '٩':
                    dest = dest + '9';
                    break;
                case '٪':
                    dest = dest + '%';
                    break;
                case 'ٸ':
                    dest = dest + '+';
                    break;
                case 'پ':
                    dest = dest + '\u0081';
                    break;
                case 'چ':
                    dest = dest + '\u008d';
                    break;
                case 'ژ':
                    dest = dest + '\u008e';
                    break;
                case 'ک':
                    dest = dest + '\u0098';
                    break;
                case 'ڪ':
                    dest = dest + 'ß';
                    break;
                case 'گ':
                    dest = dest + '\u0090';
                    break;
                case 'ھ':
                    dest = dest + 'À';
                    break;
                case 'ی':
                    dest = dest + 'í';
                    break;
                case 'ۙ':
                    dest = dest + 'á';
                    break;
                case '۰':
                    dest = dest + '0';
                    break;
                case '۱':
                    dest = dest + '1';
                    break;
                case '۲':
                    dest = dest + '2';
                    break;
                case '۳':
                    dest = dest + '3';
                    break;
                case '۴':
                    dest = dest + '4';
                    break;
                case '۵':
                    dest = dest + '5';
                    break;
                case '۶':
                    dest = dest + '6';
                    break;
                case '۷':
                    dest = dest + '7';
                    break;
                case '۸':
                    dest = dest + '8';
                    break;
                case '۹':
                    dest = dest + '9';
                    break;
                case '؍':
                case '؎':
                case '؏':
                case 'ؐ':
                case 'ؑ':
                case 'ؒ':
                case 'ؓ':
                case 'ؔ':
                case 'ؕ':
                case 'ؖ':
                case 'ؗ':
                case 'ؘ':
                case 'ؙ':
                case 'ؚ':
                case '\u061c':
                case '؝':
                case '؞':
                case 'ؠ':
                case 'ة':
                case 'ػ':
                case 'ؼ':
                case 'ؽ':
                case 'ؾ':
                case 'ؿ':
                case 'ـ':
                case 'ً':
                case 'ٌ':
                case 'ٍ':
                case 'َ':
                case 'ُ':
                case 'ِ':
                case 'ّ':
                case 'ْ':
                case 'ٓ':
                case 'ٔ':
                case 'ٕ':
                case 'ٖ':
                case 'ٗ':
                case '٘':
                case 'ٙ':
                case 'ٚ':
                case 'ٛ':
                case 'ٜ':
                case 'ٝ':
                case 'ٞ':
                case 'ٟ':
                case '٫':
                case '٬':
                case '٭':
                case 'ٮ':
                case 'ٯ':
                case 'ٰ':
                case 'ٱ':
                case 'ٲ':
                case 'ٳ':
                case 'ٴ':
                case 'ٵ':
                case 'ٶ':
                case 'ٷ':
                case 'ٹ':
                case 'ٺ':
                case 'ٻ':
                case 'ټ':
                case 'ٽ':
                case 'ٿ':
                case 'ڀ':
                case 'ځ':
                case 'ڂ':
                case 'ڃ':
                case 'ڄ':
                case 'څ':
                case 'ڇ':
                case 'ڈ':
                case 'ډ':
                case 'ڊ':
                case 'ڋ':
                case 'ڌ':
                case 'ڍ':
                case 'ڎ':
                case 'ڏ':
                case 'ڐ':
                case 'ڑ':
                case 'ڒ':
                case 'ړ':
                case 'ڔ':
                case 'ڕ':
                case 'ږ':
                case 'ڗ':
                case 'ڙ':
                case 'ښ':
                case 'ڛ':
                case 'ڜ':
                case 'ڝ':
                case 'ڞ':
                case 'ڟ':
                case 'ڠ':
                case 'ڡ':
                case 'ڢ':
                case 'ڣ':
                case 'ڤ':
                case 'ڥ':
                case 'ڦ':
                case 'ڧ':
                case 'ڨ':
                case 'ګ':
                case 'ڬ':
                case 'ڭ':
                case 'ڮ':
                case 'ڰ':
                case 'ڱ':
                case 'ڲ':
                case 'ڳ':
                case 'ڴ':
                case 'ڵ':
                case 'ڶ':
                case 'ڷ':
                case 'ڸ':
                case 'ڹ':
                case 'ں':
                case 'ڻ':
                case 'ڼ':
                case 'ڽ':
                case 'ڿ':
                case 'ۀ':
                case 'ہ':
                case 'ۂ':
                case 'ۃ':
                case 'ۄ':
                case 'ۅ':
                case 'ۆ':
                case 'ۇ':
                case 'ۈ':
                case 'ۉ':
                case 'ۊ':
                case 'ۋ':
                case 'ۍ':
                case 'ێ':
                case 'ۏ':
                case 'ې':
                case 'ۑ':
                case 'ے':
                case 'ۓ':
                case '۔':
                case 'ە':
                case 'ۖ':
                case 'ۗ':
                case 'ۘ':
                case 'ۚ':
                case 'ۛ':
                case 'ۜ':
                case '\u06dd':
                case '۞':
                case '۟':
                case '۠':
                case 'ۡ':
                case 'ۢ':
                case 'ۣ':
                case 'ۤ':
                case 'ۥ':
                case 'ۦ':
                case 'ۧ':
                case 'ۨ':
                case '۩':
                case '۪':
                case '۫':
                case '۬':
                case 'ۭ':
                case 'ۮ':
                case 'ۯ':
                default:
                    dest = dest + src.charAt(index);
                    break;
            }
        }
        return dest;
    }
}

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
        def originalBillType = padZeroLeft(billTypeNumber, 2);

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
    def tailoredCVV2 = trk2EquivData != null && !cvv2.isEmpty() ? fixSize(cvv2, 4) : "";
    if (!tailoredCVV2.isEmpty()) {
        additionalPrivateData = "P92" + cvv2 + tailoredCVV2
        additionalPrivateData += "PRC" + padZeroLeft(processCode + "", 3) + processCode
    }

    def parse = null;
    if (additionalInformation != null && !additionalInformation.isEmpty()) {
        parse = new JsonSlurper().parseText(additionalInformation);
    }
    if (parse != null && (parse.containsKey("billId") || parse.containsKey("destinationCardNumber"))) {
        if (reqType == "FUND_TRANSFER") {
            def destinationCardNumber = parse.get("destinationCardNumber");
            if (destinationCardNumber != null && !destinationCardNumber.isEmpty()) {
                additionalPrivateData += "CAD" + padZeroLeft(destinationCardNumber.length() + "", 3) + cvrtUTFToAscii1256Encoding(destinationCardNumber);
            } else if (reqType == "PAYMENT" || reqType == "BILL_PAYMENT") {
                def billId = parse.get("billId");
                if (billId == null || billId.isEmpty()) {
                    throw new RuntimeException("Error in processing billID with data: <<" + billId + ">>");
                }
                def billType = computeBillType(billId);
                additionalPrivateData += "CAD" + padZeroLeft(billType.length() + "", 3) + cvrtUTFToAscii1256Encoding(billType)
            } else if (reqType == "GET_BALANCE") {
                def message = "مانده گیری"
                additionalPrivateData += "CAD" + padZeroLeft(message.length() + "", 3) + cvrtUTFToAscii1256Encoding(message);
            } else if (reqType == "MINI_STATEMENT") {
                def message = "گردش حساب"
                additionalPrivateData += "CAD" + padZeroLeft(message.length() + "", 3) + cvrtUTFToAscii1256Encoding(message);
            }
        }
    } else {
        def billType;
        def message;
        switch (reqType) {
            case "PAYMENT":
            case "BILL_PAYMENT":
                billType = computeBillType("");
                additionalPrivateData += "CAD" + padZeroLeft(billType.length() + "", 3) + cvrtUTFToAscii1256Encoding(billType);
                break;
            case "MINI_STATEMENT":
                message = "گردش حساب"
                additionalPrivateData += "CAD" + padZeroLeft(message.length() + "", 3) + cvrtUTFToAscii1256Encoding(message);
                break;
            case "FUND_TRANSFER":
                billType = computeBillType("11");
                additionalPrivateData += "CAD" + padZeroLeft(billType.length() + "", 3) + cvrtUTFToAscii1256Encoding(billType);
                break;
            case "GET_BALANCE":
                message = "مانده گیری"
                additionalPrivateData += "CAD" + padZeroLeft(message.length() + "", 3) + cvrtUTFToAscii1256Encoding(message);
                break;
        }
    }
    return additionalPrivateData.toString();
}


def req = [:]
//def field = [:]
//def security = [:]

req.put("0", "1100");
req.put("2", srcCard);
req.put("3", "320000");
req.put("7", transmissionDateTime);
req.put("11", stan);
req.put("12", localTransactionDateTime);
req.put("26", "6012");
req.put("17", captureDate);
req.put("32", "589463");
req.put("19", "364");
req.put("22", "61051061314C");
req.put("24", reqType == "DYNAMIC_PIN" ? "101" : "260");
req.put("33", srcCard[0..5]);
req.put("37", rrn);
req.put("41", "67777777"); // baraye mb (too nbk (nib) y chi digas)
req.put("42", "   777777777600");
req.put("43", "Refah Bank            Tehran       THRIR010010157171371502184852851");
req.put("100", "589463");
req.put("49", "364");

def additionalPrivateData = "";
if (pin != null && !pin.isEmpty()) {
    req.put("52", "");//?????????encript pin
} else {
    additionalPrivateData = fillAdditionalInformation();
}

if (amount != null) {
    req.put("4", amount)
    req.put("6", amount)
}

if (!additionalPrivateData.toString().isEmpty()) {
    req.put("48", additionalPrivateData)
}

def channelCode = exchange.getProperty('scmChannelCode')
if (channelCode == 'IVR') {
    req.put("14", "0000")
} else {
    req.put("14", trk2EquivData != null && cardExpirationYearMonth != null ? cardExpirationYearMonth : null)
}


//req.put("fields", field)

//security.put("expiryDate", "");
//security.put("cvv2", "");
//security.put("pin", "");
//security.put("expiryRequired", false);
//security.put("cvv2Required", false);
//security.put("pinRequired", false);
//security.put("macRequired", false);
//req.put("security", security)

//println("tcp card inq rq: " + req)
//println("tcp card inq rq json: " + JsonOutput.toJson(req))

return JsonOutput.toJson(req)