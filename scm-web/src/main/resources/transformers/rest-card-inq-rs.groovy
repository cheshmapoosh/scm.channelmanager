package transformers

import ir.daneshrefah.scm.common.data.dto.bank.BankDto
import ir.daneshrefah.scm.common.exception.CardException
import ir.daneshrefah.scm.common.model.message.Message
import ir.daneshrefah.scm.common.transformerUtil.PersianStringUtil
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField

def bodyRaw = exchange.in.body
def body = bodyRaw.body
def errorCode = body.errorCode
def headers = exchange.in.headers
println("rest cardInquiry rs transformer start provider body : " + body)
if (!body instanceof Map) {
    return
}

def status = body.get("status")
println("rest card inq rs status : " + status)
if (errorCode != null) {
    throw new CardException(errorCode.toString(),body.errorDescription)
}
def statusCode = status as int
if (!(statusCode >= 200 && statusCode < 300)) {
    throw new RuntimeException("Expected 2xx status from HPS card inquiry endpoint, but got " + statusCode)
}

def bodyResponse = body.get("body")
def out = bodyResponse.get("outData")
println("out clas  : " + out.getClass())

if (out == null && bodyResponse.get("errorCode") != null) {
    throw new RuntimeException(bodyResponse.get("errorDescription"))
}

println("out cardInq rs : " + out)
println("rest cardInquiry rs transformer end transformed body : " + body)

def originalBody = exchange.getProperty(Message.ORIGINAL_BODY)
def destCardNumber = originalBody?.fundTransfer?.destinationCardNumber
if (destCardNumber == null && bodyResponse instanceof Map) {
    destCardNumber = bodyResponse.get("destCard") ?: bodyResponse.get("destinationCardNumber")
}
if (destCardNumber == null && out instanceof Map) {
    destCardNumber = out.get("destCard") ?: out.get("destinationCardNumber")
}
def bankPrefix = destCardNumber == null ? null : destCardNumber.toString()
bankPrefix = bankPrefix != null && bankPrefix.length() >= 6 ? bankPrefix[0..5] : null

def detection = exchange.context.registry.lookupByName("bankListLoader")
BankDto bank = bankPrefix == null ? null : detection.getBank(bankPrefix)
println("bank name : " + (bank == null ? "" : bank.getName()))


def customerNameFamily = out["destName"].toString()
println("customerNameFamily : " + customerNameFamily)
def name = "";
def family = "";

if (!(customerNameFamily.isEmpty() || customerNameFamily.length() <= 25)) {
    try {
        def tailoredCustomerNameFamily = customerNameFamily[25..-1]
        int nameLen = tailoredCustomerNameFamily[0..1] as int

        int nameStart = 2
        int nameEnd = nameStart + nameLen

        name = tailoredCustomerNameFamily[nameStart..<nameEnd]
        println("name : " + name)

        int familyLen = tailoredCustomerNameFamily[nameEnd..<(nameEnd + 2)] as int

        int familyStart = nameEnd + 2
        int familyEnd = familyStart + familyLen

        family = tailoredCustomerNameFamily[familyStart..<familyEnd]
        println("family : " + family)
    } catch (Exception e) {
        name = "";
        family = ""
    }
}

println("end name proces")
return [
        "card"        : [
                "destinationBankName": bank == null ? "" : bank.getName(),
                "imageUrl"           : ""
        ],
        "customerName": [
                "firstName": name.isEmpty() ? "" : PersianStringUtil.convertArabicToPersianUTF(PersianStringUtil.cvrtIranSystem2Utf(name)),
                "lastName" : family.isEmpty() ? "" : PersianStringUtil.convertArabicToPersianUTF(PersianStringUtil.cvrtIranSystem2Utf(family))
        ]
]
