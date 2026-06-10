import ir.daneshrefah.scm.common.data.dto.bank.BankDto
import ir.daneshrefah.scm.common.exception.CardException
import ir.daneshrefah.scm.common.model.message.Message
import ir.daneshrefah.scm.provider.shetab.iso.util.ISOField
import ir.daneshrefah.scm.provider.shetab.iso.util.MTI
import ir.daneshrefah.scm.provider.shetab.iso.util.ResponseCode
import ir.daneshrefah.scm.common.transformerUtil.PersianStringUtil

def body = exchange.in.body
println("tcp rs")

if (!(body instanceof Map)) {
    return
}

def mti = body.get("mti")
println("tcp card inq rs mti : " + mti)
if (!mti.toString().trim().equals(MTI.AUTHORIZATION_ADVICE_RESPONSE_COMMAND.getCode())) {
    throw new RuntimeException("tcp card inq rs : mti is null")
}

def fields = body.get("fields")
println("tcp card inq rs fields : " + fields)
if (fields == null) {
    throw new RuntimeException("tcp card inq rs : fields is null")
}

println("tcp card inq rs action code" + fields[ISOField.ACTION_CODE.getPosition().toString()])
if (fields[ISOField.ACTION_CODE.getPosition().toString()] == null || !fields[ISOField.ACTION_CODE.getPosition().toString()].toString().equals(ResponseCode.APPROVED.getCode())) {
    throw new CardException(fields[ISOField.ACTION_CODE.getPosition().toString()].toString(), "tcp card inq rs action code : " + fields[ISOField.ACTION_CODE.getPosition().toString()].toString())
}

def customerNameFamily = fields[ISOField.ADDITIONAL_RESPONSE_DATA.getPosition().toString()].toString()
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

        int familyLen = tailoredCustomerNameFamily[nameEnd..<(nameEnd + 2)] as int

        int familyStart = nameEnd + 2
        int familyEnd = familyStart + familyLen

        family = tailoredCustomerNameFamily[familyStart..<familyEnd]
    } catch (Exception e) {
        name = "";
        family = ""
    }
}

def originalBody = exchange.getProperty(Message.ORIGINAL_BODY)
println("origin body :"+ originalBody)
def destCardNumber = originalBody?.fundTransfer?.destinationCardNumber
println("dest card : "+ destCardNumber)

def cardNo = destCardNumber?.toString()?.replace('"','')?.trim()
def bankPrefix = cardNo?.length() >= 6 ? cardNo[0..5] : null
println("card prefix : "+ bankPrefix)
def detection = exchange.context.registry.lookupByName("bankListLoader")
BankDto bank = bankPrefix == null ? null : detection.getBank(bankPrefix)
println("bank name : " + (bank == null ? "" : bank.getName()))

println("end tcp card inquiry!")
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