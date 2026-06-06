package transformers

import ir.daneshrefah.scm.common.data.dto.bank.BankDto
import ir.daneshrefah.scm.common.model.message.Message
import ir.daneshrefah.scm.common.transformerUtil.PersianStringUtil

def body = exchange.in.body
def headers = exchange.in.headers
println("rest cardInquiry rs transformer start provider body : " + body)
if (!body instanceof Map) {
    return
}

def status = body.get("status")
println("rest card inq rs status : " + status)
if (status == null) {
    throw new RuntimeException("REST provider should set HTTP response code")
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

return [
        "card": [
                "destinationBankName": bank == null ? "" : bank.getName(),
                "imageUrl": ""
        ],
        "customerName": [
                "firstName": PersianStringUtil.convertArabicToPersianUTF(PersianStringUtil.cvrtIranSystem2Utf(out["destName"].toString())),
                "lastName" : ""
        ]
]
