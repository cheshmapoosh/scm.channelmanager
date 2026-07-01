package transformers

import ir.daneshrefah.scm.common.data.entity.asset.AccountTypeLoader

def nabResponse = exchange.in.body
println("5m nab response : "+ nabResponse)

def status = nabResponse.status
def actionCode = status.code
def success = status.success
if (!success){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}

def bodyRawList = nabResponse.records

def responseList = []
for(def body in bodyRawList){
    println("5m response body : " + body);

    def accountNo = body.accountNo
    def accountType = body.accountType
    def accountDesc = body.accountDesc
//    def accountBalance = body[119..136]
//    def accountAvailBalance = body[137..154]
    def iBanValue = body.iBanValue
    def generalCode = body.generalCode
    def descGeneral = body.descGeneral
    def subsidiaryAccount = body.subsidiaryAccount
    def descSubsidiary = body.descSubsidiary
    def commerce = body.commerce
    def typeTrans = body.typeTrans.asText()
    def maxInternalAmount = body.maxInternalAmount
    def maxPayaAmount = body.maxPayaAmount
    def maxSatnaAmount = body.maxSatnaAmount
    def maxIpAmount = body.maxIpAmount
    def expireDate = body.expireDate
    def createDate = body.createDate
    def permitServiceId = body.permitServiceId.asText()
    def remDebitFt = body.remDebitFt
    def remDebitSatna = body.remDebitSatna
    def remDebitPaya = body.remDebitPaya
    def remDebitPol = body.remDebitPol

    def privilages = []

    println("start typetrans")

    if (typeTrans[0] == "1") {
        privilages << [privilage:"XFER_ADD", amount : maxInternalAmount.asText().trim().toLong(), remDebit : remDebitFt.asText().trim().toLong()]
    }
    if (typeTrans[1] == "1") {
        privilages << [privilage:"ACH_XFER_ADD", amount :maxPayaAmount.asText().trim().toLong(), remDebit : remDebitSatna.asText().trim().toLong()]
    }
    if (typeTrans[2] == "1") {
        privilages << [privilage:"RTGS_XFER_ADD", amount :maxSatnaAmount.asText().trim().toLong(), remDebit : remDebitPaya.asText().trim().toLong()]
    }
    if (typeTrans[3] == "1") {
        privilages << [privilage:"IP_XFER_ADD", amount :maxIpAmount.asText().trim().toLong(), remDebit : remDebitPol.asText().trim().toLong()]
    }

    println("start permits")
    def permitServices = []
    if(permitServiceId[0..0] == "1"){
        permitServices << "atm"
    }
    if(permitServiceId[1..1] == "1"){
        permitServices << "mb"
    }
    if(permitServiceId[2..2] == "1"){
        permitServices << "ib"
    }

    println("start acc type")
    def accountTypeName = AccountTypeLoader.accountTypeEntityMap[accountType]?.name ?: ""

    println("start item")
    def item = [
            "accountNo":accountNo,
            "accountType":accountTypeName,
            "accountDesc":accountDesc,
//            "accountBalance":accountBalance.toString().trim().toLong(),
//            "accountAvailBalance":accountAvailBalance.toString().trim().toLong(),
            "iBanValue":iBanValue,
            "generallCode":generalCode,
            "descGenerall":descGeneral,
            "subsidiaryAccount":subsidiaryAccount,
            "descSubsidiary":descSubsidiary,
            "commerce":commerce,
            "typeTrans":privilages,
            "expireDate":expireDate,
            "createDate":createDate,
            "permitServiceId" : permitServices
    ]
    responseList << item
}

println("5m transformed responseList : "+ responseList)
return responseList