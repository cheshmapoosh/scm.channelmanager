package transformers

import ir.daneshrefah.scm.common.constant.CacheConstants
import ir.daneshrefah.scm.common.data.dto.bank.BankDto
import ir.daneshrefah.scm.common.exception.CardException
import ir.daneshrefah.scm.common.model.message.Message
import ir.daneshrefah.scm.common.transformerUtil.PersianStringUtil
import ir.daneshrefah.scm.provider.shetab.iso.util.CardConstant
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField

def bodyRaw = exchange.in.body
def body = bodyRaw.body
def errorCode = body.errorCode
def headers = exchange.in.headers
println("rest cardInquiry rs transformer start provider body : " + body)
if (!body instanceof Map) {
    return
}

def status = body.get("succeed")
println("rest card inq rs status : " + status)
if (errorCode != null) {
    throw new CardException(errorCode.toString(),body.errorDescription)
}

def out = body.get("outData")
println("out clas  : " + out.getClass())

if (out == null && status == false) {
    throw new RuntimeException(body.get("errorDescription"))
}

println("out cardInq rs : " + out)
println("rest cardInquiry rs transformer end transformed body : " + body)

def originalBody = exchange.getProperty(Message.ORIGINAL_BODY)
println("origin body :"+ originalBody)
def destCardNumber = originalBody?.fundTransfer?.destinationCardNumber
println("dest card : "+ destCardNumber)

def originSrcCard = originalBody?.fundTransfer?.sourceCardNumber
def srcCardNo = originSrcCard?.toString()?.replace('"','')?.trim()

def cardNo = destCardNumber?.toString()?.replace('"','')?.trim()
def bankPrefix = cardNo?.length() >= 6 ? cardNo[0..5] : null
println("card prefix : "+ bankPrefix)
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

def customerName = name.isEmpty() ? "" : PersianStringUtil.convertArabicToPersianUTF(PersianStringUtil.cvrtIranSystem2Utf(name))
def customerLastName = family.isEmpty() ? "" : PersianStringUtil.convertArabicToPersianUTF(PersianStringUtil.cvrtIranSystem2Utf(family))

def cache = exchange.context.registry.lookupByName("transformerCacheManager");
cache.putInCache(
        CacheConstants.CACHE_NAME_DEST_CARD_CUS,
        srcCardNo==null ? "":srcCardNo + ":" + CardConstant.DEFAULT_CARD_ACCEPT_TERMINAL_ID,
        customerName.toString().concat(customerLastName.toString()))

println("end name proces")
return [
        "card"        : [
                "destinationBankName": bank == null ? "" : bank.getName(),
                "imageUrl"           : ""
        ],
        "customerName": [
                "firstName": customerName,
                "lastName" : customerLastName
        ]
]
