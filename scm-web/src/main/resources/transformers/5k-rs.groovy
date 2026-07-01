package transformers

import ir.daneshrefah.scm.common.data.entity.asset.AccountTypeLoader

def nabResponse = exchange.in.body
println("5k nab response : "+ nabResponse)

def status = nabResponse.status
def actionCode = status.code
def success = status.success
if (!success){
    throw new ir.daneshrefah.scm.common.exception.NabError(actionCode.asText(), "nab error!");
}

def bodyRawList = nabResponse.records
def responseList = []
for (def body in bodyRawList) {
    println("5k response body : " + body);

    def nationalId = body.nationalId
    def name = body.name
    def lastName = body.lastName
    def mobile = body.mobile
    def address = body.address
    def accountType = body.accountType
    def generalCode = body.general
    def descGeneral = body.descGeneral
    def subsidiaryCode = body.subsidiary
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

    println("accounttype : "+ accountType)
    println("all account type :" + AccountTypeLoader.accountTypeEntityMap.keySet())
    def accountTypeName = AccountTypeLoader.accountTypeEntityMap[accountType]?.name ?: ""
    println("accountTypeName ": accountTypeName)

    def privilages = []

    println("typeTrans : "+ typeTrans)
    println("typeTrans 0: "+ typeTrans[0])
    println("typeTrans 1: "+ typeTrans[1])
    println("typeTrans 2: "+ typeTrans[2])
    println("typeTrans 3: "+ typeTrans[3].getClass())
    if (typeTrans[0] == "1") {
        privilages << [privilage:"XFER_ADD", amount : maxInternalAmount.asText().trim().toLong()]
    }
    if (typeTrans[1] == "1") {
        privilages << [privilage:"ACH_XFER_ADD", amount :maxPayaAmount.asText().trim().toLong()]
    }
    if (typeTrans[2] == "1") {
        privilages << [privilage:"RTGS_XFER_ADD", amount :maxSatnaAmount.asText().trim().toLong()]
    }
    if (typeTrans[3] == "1") {
        privilages << [privilage:"IP_XFER_ADD", amount :maxIpAmount.asText().trim().toLong()]
    }

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

    def item = [
            "nationalId"        : nationalId,
            "name"              : name,
            "lastName"          : lastName,
            "mobile"            : mobile,
            "address"           : address,
            "accountType"       : accountTypeName,
            "generalCode"      : generalCode,
            "descGeneral"      : descGeneral,
            "subsidiaryCode"    : subsidiaryCode,
            "descSubsidiary"    : descSubsidiary,
            "commerce"          : commerce,
            "typeTrans"         : privilages,
            "expireDate"        : expireDate,
            "createDate"        : createDate,
            "permitServiceId"   : permitServices
    ]
    responseList << item
}
return responseList